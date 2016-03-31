package helpers

import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.ExecutionContext

// Get the db-executionContext (fork-join-executor & its config) from appplication.conf
object Contexts {
  val database: ExecutionContext = Akka.system.dispatchers.lookup("contexts.database")
}
