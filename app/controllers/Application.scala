package controllers

import javax.inject._
import play.api._
import play.api.mvc._

// Inject play.api.Configuration so we can use it to obtain the string for application.conf param "text":
class Application @Inject() (configuration: Configuration) extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Moikka!! Hello!!"))  //becomes the title-string of main.scala.html
  }

  def text = Action {
    Ok(configuration.getString("text").getOrElse("Hello world [default text]"))
  }

}
