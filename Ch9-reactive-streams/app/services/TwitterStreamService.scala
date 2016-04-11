package services

import javax.inject._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import org.reactivestreams.Publisher
import play.api._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.oauth._
import play.api.libs.streams.Streams
import play.api.libs.ws._
import play.extras.iteratees._

import scala.concurrent.ExecutionContext

class TwitterStreamService @Inject() (
  ws: WSAPI,
  system: ActorSystem,
  executionContext: ExecutionContext,
  configuration: Configuration
) {

  // Build the Enumerator which streams the parsed Tweets
  private def buildTwitterEnumerator(
    consumerKey: ConsumerKey,
    requestToken: RequestToken,
    topics: Seq[String]
  ): Enumerator[JsObject] = {
    
    // Create simple adapter (tuple of Iteratee and Enumerator):
    val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
    val url = "https://stream.twitter.com/1.1/statuses/filter.json"
    implicit val ec = executionContext

    // Format the topics we intend to track:
    val formattedTopics = topics.map(t => "#" + t).mkString(",")

    ws
      .url(url)
      .sign(OAuthCalculator(consumerKey, requestToken))
      .postAndRetrieveStream(  
         // Send a POST request and fetch the stream from Twitter. 
         // This method expects to be fed a body as well as be given a consumer
        Map("track" -> Seq(formattedTopics))
      ) { response =>
        Logger.info("Status: " + response.status)
        // Pass in the Iteratee as a consumer. The stream will flow through this Iteratee to the joined Enumerator
        iteratee
      }.map { _ =>
        Logger.info("Twitter stream closed")
      }

    // Transform stream by decoding and parsing it:
    val jsonStream: Enumerator[JsObject] = enumerator &>
      Encoding.decode() &>
      Enumeratee.grouped(JsonIteratees.jsSimpleObject)

    // Return transformed stream as an Enumerator[JsObject]:
    jsonStream
  }//def-buildTwitterEnumerator

  // Convert Enumerator to a Source:
  private def enumeratorToSource[Out] (
    enum: Enumerator[Out]
  ): Source[Out, Unit] = {
    // Enumerator to reactive-streams-Publisher:
    val publisher: Publisher[Out] = Streams.enumeratorToPublisher(enum)
    // Publisher to Akka-streams-Source:
    Source(publisher)
  }//def-enumeratorToSource

  // Create "SplitByTopic"-junction using FlexiRoute.
  // we want to get an Enumerator as a result in order to feed it into a WebSocket connection
  def stream(topicsAndDigestRate: Map[String, Int]): Enumerator[JsValue] = {
    import FanOutShape._
    // Defining the shape of our custom junction by extending FanOutShape. Since this is
    // a fan out junction, we only describe the output ports (or outlets) since there is only one input port.
    class SplitByTopicShape[A <: JsObject](
      _init: Init[A] = Name[A]("SplitByTopic")
    ) extends FanOutShape[A](_init) {

      protected override def construct(i: Init[A]) = new SplitByTopicShape(i)
      // Creating one output port per topic and keeping these ports in a map so that we can
      // retrieve them by topic later:
      val topicOutlets = topicsAndDigestRate.keys.map { topic =>
        topic -> newOutlet[A]("out-" + topic)
      }.toMap
    }
  
    // Defining our custom junction by extending FlexiRoute
    class SplitByTopic[A <: JsObject]
    extends FlexiRoute[A, SplitByTopicShape[A]](
      new SplitByTopicShape, Attributes.name("SplitByTopic")
    ) {
      import FlexiRoute._

      // Defining the routing logic of our junction where we will define how elements get routed
      override def createRouteLogic(p: PortT) = new RouteLogic[A] {
        // Extracting the first topic out of a tweet. We will effectively split using only the first
        // topic in this example:
        def extractFirstHashTag(tweet: JsObject) = 
          (tweet \ "entities" \ "hashtags")
            .asOpt[JsArray]
            .flatMap { hashtags =>
              hashtags.value.headOption.map { hashtag =>
                (hashtag \ "text").as[String]
              }
            }

        override def initialState = 
          // Specifying the demand condition that we want to use. In our case we trigger when
          // any of the outward streams is ready to receive more elements:
          State[Any](DemandFromAny(p.topicOutlets.values.toSeq :_*)) {
            (ctx, _, element) =>
              // Using the first hash of a tweet to route it to the appropriate port, and ignoring Tweets
              // that do not match:
              extractFirstHashTag(element).foreach { topic =>
                p.topicOutlets.get(topic).foreach { port =>
                  ctx.emit(port)(element)
                }
              }
              SameState
          }
      
        override def initialCompletionHandling = eagerClose
 
      }//def-createRouteLogic
    }//class-SplitByTopic

    //Enumerator.empty[JsValue] //temp before 
    // Listing 9.5 Adding all required elements to our graph with the FlowGraph builder

    credentials.map { case (consumerKey, requestToken) =>
      // Need a FlowMaterialiser to actually run the graph-flow:
      implicit val fm = ActorMaterializer()(system)
      
      // Build the enumerator-source:
      val enumerator = buildTwitterEnumerator(consumerKey, requestToken, topicsAndDigestRate.keys.toSeq)

      // Defining a Sink where the data will flow to. We use a Sink that will produce a Reactive Streams Publisher:
      val sink = Sink.publisher[JsValue]

      // Create the builder for a closed FlowGraph, passing in our sink as output value that will be materialized when the flow is run:
      val graph = FlowGraph.closed(sink) { implicit builder => out =>
        // Add the source to the graph:
        val in = builder.add(enumeratorToSource(enumerator))

        // Add our splitter to the graph:
        val splitter = builder.add(new SplitByTopic[JsObject])

        // Add the groupers to the graph, one for each topic. 
        // These will group N elements together depending on the digest-rate of each topic:
        val groupers = topicsAndDigestRate.map { case (topic, rate) =>
          topic -> builder.add(Flow[JsObject].grouped(rate))
        }

        // Add the taggers to the graph, one for each topic. These will take the grouped
        // tweets and build one JSON object out of them, tagging it with the topic:
        val taggers = topicsAndDigestRate.map { case (topic, _) =>
          topic -> {
            val t = Flow[Seq[JsObject]].map { tweets => 
              Json.obj("topic" -> topic, "tweets" -> tweets)
            }
            builder.add(t)
          }
        }

        // Add a merger to the graph responsible for merging all streams back together:
        val merger = builder.add(Merge[JsValue](topicsAndDigestRate.size))

        // Wire up the graph:
        // Connect our source to splitter's inlet:
        builder.addEdge(in, splitter.in)
        // Do wiring foreach splitter-outlet:
        splitter.topicOutlets.zipWithIndex.foreach { case ((topic, port), index) =>
          val grouper = groupers(topic)
          val tagger = taggers(topic)
          // Connect splitter-outlet(i.e. a substream) to topic-grouper:
          builder.addEdge(port, grouper.inlet)
          // Connect grouper-outlet to tagger-inlet:
          builder.addEdge(grouper.outlet, tagger.inlet)
          // Connect tagger-outlet to one of the merger-ports:
          builder.addEdge(tagger.outlet, merger.in(index))
        }

        // Connect merger-outlet to (output!!-)Publisher-inlet:
        builder.addEdge(merger.out, out.inlet)
        //--to slow down the flow, can add into TwitterStreamService just before passing stream to Sink (replacing "builder.addEdge(merger.out, out.inlet)", the following:
        /**
        val sleeper = builder.add(Flow[JsValue].map { element =>
          Thread.sleep(5000)
          element
        })
        builder.addEdge(merger.out, sleeper.inlet)
        builder.addEdge(sleeper.outlet, out.inlet) */
        // Leads to this kind of output with e.g. http://localhost:9000/?topic=cats:1&topic=dogs:2
        // [info] - application - Status: 200
        // [info] - application - Status: 420
        // [info] - application - Twitter stream closed
        // etymology of the http-status-code 420 acc. wikipedia: https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#4xx_Client_Error
        // 420 Enhance Your Calm (Twitter)
        // Returned by version 1 of the Twitter Search and Trends API when the client is being rate limited;


      }//graph
      
      // Run the graph. The materialized result will be the Publisher that we can convert back to an Enumerator:
      val publisher = graph.run()
      Streams.publisherToEnumerator(publisher)

    } getOrElse {
      Logger.error("Twitter credentials are not configured right")
      Enumerator.empty[JsValue]
    }//credentials.map

  }//def-stream

  private def credentials = for {
    apiKey <- configuration.getString("twitter.apiKey")
    apiSecret <- configuration.getString("twitter.apiSecret")
    token <- configuration.getString("twitter.accessToken")
    tokenSecret <- configuration.getString("twitter.accessTokenSecret")
  } yield
    (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))

}






















