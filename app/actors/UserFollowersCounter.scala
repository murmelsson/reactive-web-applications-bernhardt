package actors

import akka.actor.{Actor, ActorLogging, Props}

class UserFollowersCounter extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("hello, world (ufc).")

  def receive = {
    case message =>   // do nothing
  }

}
