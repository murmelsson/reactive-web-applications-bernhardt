package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}

class TweetReachComputer(userFollowersCounter: ActorRef, storage: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("hello, world (trc).")

  def receive = {
    case message =>   // do nothing
  }

}

object TweetReachComputer {
  def props(followersCounter: ActorRef, storage: ActorRef) = 
    Props(classOf[TweetReachComputer], followersCounter, storage)
}
