package actors

import akka.actor.{Actor, ActorLogging, Props}
//import StatisticsProvider._

class StatisticsProvider extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("hello, world!")

  def receive = {
    case message =>   // do nothing
  }

}

//object StatisticsProvider {
//  def props = Props[StatisticsProvider]
//}
