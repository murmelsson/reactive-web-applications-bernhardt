package controllers

import javax.inject._

import actors.StatisticsProvider
import akka.actor.ActorSystem
import akka.util.Timeout
import messages.{TweetReachCouldNotBeComputed, TweetReach, ComputeReach}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import akka.pattern.ask
import scala.concurrent.duration._

class Application @Inject() (system: ActorSystem) extends Controller {

  lazy val statisticsProvider = system.actorSelection("akka://application/user/statisticsProvider")

  def computeReach(tweetId: String) = Action.async {
    implicit val timeout = Timeout(5.minutes)
    val eventuallyReach = statisticsProvider ? ComputeReach(BigInt(tweetId))
    eventuallyReach.map {
      case tr: TweetReach =>
        println("Yes! TweetReach found for tweetId: " + ", score: " + tr.score)
        Ok(tr.score.toString)
      case StatisticsProvider.ServiceUnavailable =>
        ServiceUnavailable("Sorry")
      case TweetReachCouldNotBeComputed =>
        ServiceUnavailable("Sorry")
    }
  }

}



// Older build:
/**package controllers

import play.api._
import play.api.mvc._

import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.ws._
import play.api.libs.iteratee._
import play.api.Logger

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def tweets = Action.async {
    val loggingIteratee = Iteratee.foreach[Array[Byte]] { array =>
      Logger.info(array.map(_.toChar).mkString)
    }

    credentials.map { case (consumerKey, requestToken) =>
      WS
        .url("https://stream.twitter.com/1.1/statuses/filter.json")
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "reactive")
        .get { response =>
          Logger.info("Status: " + response.status)
          loggingIteratee
        }.map { _ =>
          Ok("Stream closed")
        }
      } getOrElse {
        Future {
          InternalServerError("Twitter credentials missing")
        }
      }
  }


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

*/
