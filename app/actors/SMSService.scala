package actors

import javax.inject.Inject

import akka.actor.{ActorLogging, Actor, Props}
import com.google.inject.AbstractModule
import helpers.Database
import play.api.libs.concurrent.AkkaGuiceSupport


class SMSService @Inject() (database: Database) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.actorOf(Props[SMSServer])
    context.actorOf(Props[CQRSCommandHandler], name = "commandHandler")
  }

  def receive = {
    case message =>
      log.info("SMSService received msg: {}", message)
  }

}


class SMSServiceModule extends AbstractModule with AkkaGuiceSupport {
  // Binding for SMSService-Actor with name="sms", this name used both for naming the
  // binding as well as the SMSServiceActor itself:
  def configure(): Unit = bindActor[SMSService]("sms")
}
