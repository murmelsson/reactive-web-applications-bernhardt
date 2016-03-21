package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.Play.current

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws._
import play.api.libs.iteratee._
import play.api.Logger
import play.api.libs.json._
import play.extras.iteratees._
import actors.TwitterStreamer
import play.api.libs.streams._
import play.api.libs.streams.ActorFlow.actorRef
import play.api.mvc.WebSocket.MessageFlowTransformer

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (env: Environment, ws: WSClient) extends Controller {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }
  def index = Action { implicit request =>
    Ok(views.html.index("Tweets"))
  }

  /**
    * tweets uses Twitter API to retrieve tweets about whatever string we put in the tracking-query.
    */
//  def tweets = Action.async {   //def tweets: Action[AnyContent]
//
//    val loggingIteratee = Iteratee.foreach[Array[Byte]] { array =>
//      Logger.info(array.map(_.toChar).mkString)
//    }
//
//    credentials.map { case (consumerKey, requestToken) =>
//      ws
//        .url("https://stream.twitter.com/1.1/statuses/filter.json")
//        .sign(OAuthCalculator(consumerKey, requestToken))
//        .withQueryString("track" -> "reactive")
//        .get { response =>
//          Logger.info("Status: " + response.status)
//          loggingIteratee
//        }.map { _ =>
//        Ok("Stream closed")
//      }
//    } getOrElse {
//      Future {
//        InternalServerError("Twitter credentials missing")
//      }
//
//    }
//  }

//  def tweets = Action.async {   //compiles, but crashes with NoSuchMethod at runtime (get needs to be reworked maybe)
//    credentials.map { case (consumerKey, requestToken) =>
//      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
//      val jsonStream: Enumerator[JsObject] =
//        enumerator &>
//          Encoding.decode() &>
//          Enumeratee.grouped(JsonIteratees.jsSimpleObject)
//      val loggingIteratee = Iteratee.foreach[JsObject] { value =>
//        Logger.info(value.toString)
//      }
//      jsonStream run loggingIteratee
//      ws
//        .url("https://stream.twitter.com/1.1/statuses/filter.json")
//        .sign(OAuthCalculator(consumerKey, requestToken))
//        .withQueryString("track" -> "reactive")
//        .get { response =>
//          Logger.info("Status: " + response.status)
//          iteratee
//        }.map { _ =>
//        Ok("Stream closed")
//      }
//    } getOrElse {
//      Future {
//        InternalServerError("Twitter credentials missing")
//      }
//    }
//  }

  def tweets = WebSocket.accept[String, JsValue](request =>
    ActorFlow[String, JsValue, _]) (request => out => TwitterStreamer.props(out))

    //request => out => TwitterStreamer.props(out)


  def credentials: Option[(ConsumerKey, RequestToken)] = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.token")
    tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
  } yield (
    ConsumerKey(apiKey, apiSecret),
    RequestToken(token, tokenSecret)
    )

}