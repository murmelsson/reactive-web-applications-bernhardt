package actors

import akka.actor.{Actor, ActorLogging, Props}

class Storage extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("hello, world (stor).")

  def receive = {
    case message =>   // do nothing
  }

}
