package controllers

import javax.inject.Inject

import helpers.Database
import play.api.mvc.{ActionBuilder, Request, Result, Results}

import scala.concurrent.Future
import play.api.db._
import play.api.i18n.{I18nSupport, MessagesApi}
import org.jooq.{DSLContext, SQLDialect}   //jooq-code moved out of Application.scala
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.libs.Crypto
import play.api.cache.Cache

class Application @Inject() (val db: Database, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  def index = Authenticated { request =>
    Ok(views.html.index(request.firstName))
  }

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

  /**
    * good advice from the book (p199 or so):
    * "a JDBC connection is not thread-safe in the sense that the database would most likely not know what to do if
    * several threads were happening to talk to the same connection concurrently. Therefore if you want to use a JDBC
    * connection in combination with Futures, make sure to access the connection inside of the Future
    * and not the other way around"
    *
    * @return  Action[AnyContent]
    */
  def authenticate = Action.async { implicit request =>
    //Bind submitted form based on request-body:
    loginForm.bindFromRequest.fold(
/*      play.api.data.Form
    def fold[R](hasErrors: (Form[T]) => R,
                success: (T) => R): R
    Handles form results. Either the form has errors, or the submission was a success and a concrete value is available. For example:
    anyForm.bindFromRequest().fold(
      f => redisplayForm(f),
      t => handleValidFormSubmission(t)
    )
    Parameters:
      hasErrors - a function to handle forms with errors
      success - a function to handle form submission success
    Type parameters:
      <R> - common result type
    Returns:
      a result R.*/
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
            Redirect(routes.Application.index()).withSession(  //withSession allocates new session with key-value pairs
              USER.ID.getName -> u.getId.toString,
              USER.FIRSTNAME.getName -> u.getFirstname,
              USER.LASTNAME.getName -> u.getLastname
            )
          } getOrElse {
            BadRequest(
              views.html.login(
                loginForm.withGlobalError("Wrong username or password")
              )
            )
          }//user.map
        }//sql:DSLContext
    )//fold
  }
}

// Define the custom request (parametrized with A to account for different types of request bodies):
case class AuthenticatedRequest[A] (userId: Long, firstName: String, lastName: String)

// Define a new Action using Play's ActionBuilder:
object Authenticated extends ActionBuilder[AuthenticatedRequest] with Results {
  //Results trait enables us to use Redirect-result later on

  //invokeBlock is called by Play when an Action is called.
  //We provide the authd-req for the block fn-param inside this method.
  override def invokeBlock[A]
  (request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {

    val authenticated = for {
      id <- request.session.get(USER.ID.getName)   //use jOOQ-generated-code to get fieldnames from session
      firstName <- request.session.get(USER.FIRSTNAME.getName)
      lastName <- request.session.get(USER.LASTNAME.getName)
    } yield {
      AuthenticatedRequest[A](id.toLong, firstName, lastName)  //build authd-req from session-yield
    }

    authenticated.map { authenticatedRequest =>
      block(authenticatedRequest)   //invoke body of authd-action by passing in our auth-req
    } getOrElse {
      Future.successful {
        Redirect(routes.Application.login()).withNewSession  //in 'OrElse'-case (where session did not contain required parameters) we redirect to login-page with new-session, thus invalidating any old session with its erroneous data
      }
    }//authdReq
  }//def invokeBlock

  def fetchUser(id: Long) =   {  //should return : Option[UserRecord]
  //Retrieve the user from Cache using id as key
    Cache.getAs[UserRecord](id.toString).map { user =>
      Some(user)
    } getOrElse {
      //query for the user from db in case of cache-miss:
      DB.withConnection { connection =>
        val sql = DSL.using(connection, SQLDialect.POSTGRES_9_3)
        val userFromDBOpt = Option(
          sql
            .selectFrom[UserRecord](USER)
            .where(USER.ID.equal(id))
            .fetchOne())
        userFromDBOpt.foreach { u =>
          Cache.set(u.getId.toString, u) //set retrieved user to the Cache
        }
        userFromDBOpt
      }//conn
    }//orElse
  }//def-fetchUser

}//obj Authenticated
