package actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
//import StatisticsProvider._

class StatisticsProvider extends Actor with ActorLogging {
  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  override def preStart(): Unit = {
    log.info("hello, world! Starting StatisticsProvider.")
    followersCounter = context.actorOf(Props[UserFollowersCounter], name = "userFollowersCounter")
    storage = context.actorOf(Props[Storage], name = "storage")
    reachComputer = context.actorOf(TweetReachComputer.props(followersCounter, storage),
                                      name = "tweetReachComputer")
  }

  def receive = {
    case message =>   // do nothing
  }

}

//object StatisticsProvider {
//  def props = Props[StatisticsProvider]
//}
