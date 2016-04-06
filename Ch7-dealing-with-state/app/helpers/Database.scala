package helpers


//import java.sql.Connection
import javax.inject.Inject

import org.jooq.{DSLContext, SQLDialect}
import org.jooq.impl.DSL
//import play.api.Play.current

import scala.concurrent.Future

class Database @Inject() (db: play.api.db.Database) {
  //define higher-order fn parameterised in A, which then takes a fn from connection-to-A as argument, returns Future[A]
  def query[A](block: DSLContext => A): Future[A] = Future {
    db.withConnection { connection =>
      val sql = DSL.using(connection, SQLDialect.POSTGRES_9_3)  //Create jOOQ DSLCOntext
      block(sql)      //invoke the param-fn in context of this db-connection by passing in hte DSLContext (sql)
    }//conn
  } (Contexts.database)  //Explicitly pass our custom db-execution-context for the Future to execute against
                         // i.e. val database: ExecutionContext = Akka.system.dispatchers.lookup("contexts.database")
                         // - recall what the compiler says if we forget to do this:
                         // "Cannot find an implicit ExecutionContext. ...pass an (implicit ec: ExecutionContext)
                         // parameter to your method or import scala.concurrent.ExecutionContext.Implicits.global"

  def withTransaction[A](block: DSLContext => A): Future[A] = Future {
    db.withTransaction { connection =>
      val sql = DSL.using(connection, SQLDialect.POSTGRES_9_3)
      block(sql)
    }
  }(Contexts.database)

}
