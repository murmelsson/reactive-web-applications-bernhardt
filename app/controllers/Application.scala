package controllers

import javax.inject.Inject
import play.api.db._
import play.api.i18n.{I18nSupport, MessagesApi}
import org.jooq.{DSLContext, SQLDialect}
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._
import play.api.mvc.{Action, Controller}
//import play.api.Play.current

class Application @Inject() (val db: Database, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  def login = Action { request =>
    db.withConnection { connection =>  //initialise db-conn using Play's own db API
      val sql: DSLContext = DSL.using(connection, SQLDialect.POSTGRES_9_3)  //create jOOQ DSL ctxt using the transaction
      val users = sql.selectFrom[UserRecord](USER).fetch()  //fetch all users into classes of type UserRecord (that were generated using generateJOOQ
      Ok(users.toString)  //display query-result as the http response
    }
  }
}
