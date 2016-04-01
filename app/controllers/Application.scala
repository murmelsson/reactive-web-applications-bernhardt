package controllers

import javax.inject.Inject

import helpers.Database

import scala.concurrent.Future
//import play.api.db._   //using helpers.Database instead (after Listing 7.11)
import play.api.i18n.{I18nSupport, MessagesApi}
import org.jooq.{DSLContext, SQLDialect}
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.Crypto

class Application @Inject() (val db: Database, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  /** code-state after Listing7.7 before any login form, just test that can read from DB:
    * def login = Action { request =>
    * db.withConnection { connection =>  //initialise db-conn using Play's own db API
    * val sql: DSLContext = DSL.using(connection, SQLDialect.POSTGRES_9_3)  //create jOOQ DSL ctxt using the transaction
    * val users = sql.selectFrom[UserRecord](USER).fetch()  //fetch all users into classes of type UserRecord (that were generated using generateJOOQ
    * Ok(users.toString)  //display query-result as the http response
    * }
    * } */
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  //login-form with email and pwd:
  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> text
    )
  )

  def authenticate = Action.async { implicit request =>
    //Bind submitted form based on request-body:
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful {     //def successful[T](result: T): Future[T]
                                //Creates an already completed Future with the specified result.
          BadRequest(views.html.login(formWithErrors)) //display login-form again with validation errors
        },
      login =>
        db.query { sql =>        //helpers.Database  def query[A](block: (DSLContext) => A): Future[A] ; sql: DSLContext
          //val sql: DSLContext = DSL.using(connection, SQLDialect.POSTGRES_9_3)  //now defined via helpers.Database
          val user = Option(sql
            .selectFrom[UserRecord](USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(Crypto.sign(login._2)))
            .fetchOne())

          user.map { u =>
            Ok(s"Hello ${u.getFirstname}")
          } getOrElse {
            BadRequest(
              views.html.login(
                loginForm.withGlobalError("Wrong username or password")
              )
            )
          }//user.map
        }//sql
    )//fold
  }
}
