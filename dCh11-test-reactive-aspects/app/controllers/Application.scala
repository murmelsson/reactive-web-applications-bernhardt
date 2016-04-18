package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.libs.ws.WSClient
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor._
import akka.util.Timeout
import akka.pattern.{ask, AskTimeoutException}
import actors._    //as in our package's stuff
import actors.RandomNumberFetcher._

class Application @Inject() (ws: WSClient, 
                             ec: ExecutionContext,
                             system: ActorSystem) extends Controller {

  implicit val executionContext = ec
  implicit val timeout = Timeout(2000.millis) 

  //Instantiate our fetcher-actor:
  val fetcher = system.actorOf(RandomNumberFetcher.props(ws))

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def compute = Action.async { implicit request =>
    //akka-ask fetcher for random number in [0,10]:
    (fetcher ? FetchRandomNumber(10)).map {
      case RandomNumber(r) =>
        Redirect(routes.Application.index())
          //pass result to 'flash-scope' of the response:
          .flashing("result" -> s"The result is $r")
      case other =>
        InternalServerError
    } recover {
      case to: AskTimeoutException =>
        Ok("Oops, something went wrong. We could be suffering a denial-of-this-amazing-service, or, the computer is too monged out to be bothered helping. Or something.")
    }
  }

}
