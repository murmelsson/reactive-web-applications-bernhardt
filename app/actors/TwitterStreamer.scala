package actors

import java.nio.file.WatchService

import akka.actor.{Actor, ActorRef, Props}
import play.api.{Logger, Play}
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator, Iteratee}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WS
import play.extras.iteratees.{Encoding, JsonIteratees}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

class TwitterStreamer(out: ActorRef) extends Actor {
  def receive = {
    case "subscribe" =>
      Logger.info("A client subscribed to the Twitter-Stream")
      //out ! Json.obj("text" -> "Hello, world!")
      TwitterStreamer.subscribe(out)
  }
}

// Companion object constructs a new TwitterStreamer using the client's ActorRef,
// and returns it wrapped in its Props (which is kind-of the metadata of an Actor)
object TwitterStreamer {
  def props(out: ActorRef) = Props(new TwitterStreamer(out))

  private var broadcastEnumerator: Option[Enumerator[JsObject]] = None

  def connect(): Unit = {
    credentials.map { case (consumerKey, requestToken) =>
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]

      val jsonStream: Enumerator[JsObject] = enumerator &>
        Encoding.decode() &>
        Enumeratee.grouped(JsonIteratees.jsSimpleObject)

      val (be, _) = Concurrent.broadcast(jsonStream)
      broadcastEnumerator = Some(be)

      //val url = "https://stream.twitter.com/1.1/statuses/filter.json"
      val maybeMasterNodeUrl = Option(System.getProperty("masterNodeUrl"))
      val url = maybeMasterNodeUrl.getOrElse {
        "https://stream.twitter.com/1.1/statuses/filter.json"
      }
      WS
        .url(url)
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "cats")
        .get { response =>
          Logger.info("Status: " + response.status)

          iteratee
        }.map { _ =>
          Logger.info("Twitter stream closed")
        }
    } getOrElse {
      Logger.error("Oops, something went wrong with Twitter-API credentials.")
    }
  }

  // Moved this method from Application to companion object of TwitterStreamer for Listing 2.12:
  def credentials: Option[(ConsumerKey, RequestToken)] = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.token")
    tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
  } yield (
    ConsumerKey(apiKey, apiSecret),
    RequestToken(token, tokenSecret))

  def subscribe(out: ActorRef): Unit = {
    if (broadcastEnumerator.isEmpty) {
      connect()
    }
    val twitterClient = Iteratee.foreach[JsObject] { t => out ! t }
    broadcastEnumerator.map { enumerator =>
      enumerator run twitterClient
    }
  }

  def subscribeNode: Enumerator[JsObject] = {
    if (broadcastEnumerator.isEmpty) {
      connect()
    }
    broadcastEnumerator.getOrElse {
      Enumerator.empty[JsObject]
    }
  }
}










