package controllers

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

class Application extends Controller {

  def index = Action { implicit request =>     //request: Request[AnyContent]
    Ok(views.html.index("Tweets"))
  }

  // Listing 2.7 implementation of tweets():
/*  def tweets = Action.async {
    credentials.map { case (consumerKey, requestToken) =>
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
      val jsonStream: Enumerator[JsObject] =
        enumerator &>
          Encoding.decode() &>
          Enumeratee.grouped(JsonIteratees.jsSimpleObject)
      val loggingIteratee = Iteratee.foreach[JsObject] { value =>
        Logger.info(value.toString)
      }
      jsonStream run loggingIteratee
      WS
        .url("https://stream.twitter.com/1.1/statuses/filter.json")
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "reactive")
        .get { response =>
          Logger.info("Status: " + response.status)
          iteratee
        }.map { _ =>
        Ok("Stream closed")
      }
    } getOrElse {
      Future {
        InternalServerError("Couldn't retrieve Twitter-API credentials.")
      }
    }
  }*/

  // Listing 2.9 implementation of tweets()-method, returns: WebSocket[String, JsValue]
  def tweets = WebSocket.acceptWithActor[String, JsValue] {    //[String In, Json-object Out]
    request => out => TwitterStreamer.props(out)   //f:(RequestHeader) => WebSocket.HandlerProps
  }                                                // or can say f: RequestHeader => ActorRef => Props



}
