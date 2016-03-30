package actors

//import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import akka.actor._
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import reactivemongo.core.errors.ConnectionException
import scala.concurrent.duration._
import StatisticsProvider._
import messages.ComputeReach
import org.joda.time.{Interval, DateTime}

class StatisticsProvider extends Actor with ActorLogging {
  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  implicit val ec = context.dispatcher

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 2.minutes){
      case _: ConnectionException =>
        Restart
      case t: Throwable =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
  }

  override def preStart(): Unit = {
    log.info("hello, world! Starting StatisticsProvider.")
    followersCounter = context.actorOf(Props[UserFollowersCounter], name = "userFollowersCounter")
    storage = context.actorOf(Props[Storage], name = "storage")
    reachComputer = context.actorOf(TweetReachComputer.props(followersCounter, storage),
                                      name = "tweetReachComputer")
    context.watch(storage)
  }

  def receive = {
    case reach: ComputeReach =>
      println("SP-rec-ComputeReach")
      reachComputer forward reach
    case Terminated(terminatedStorageRef) =>
      // only Terminated case is when watching storage. Schedule sending a ReviveStorage to this Actor itSelf, and switch state to storageUnavailable's receiver-behaviour:
      context.system.scheduler.scheduleOnce(1.minute, self, ReviveStorage)
      context.become(storageUnavailable)
    case TwitterRateLimitReached(reset) =>
      context.system.scheduler.scheduleOnce(
        new Interval(DateTime.now, reset).toDurationMillis.millis,
        self,
        ResumeService
      )
      context.become(serviceUnavailable)
  }

  def storageUnavailable: Receive = {
    case ComputeReach(_) =>
      sender() ! ServiceUnavailable
    case ReviveStorage =>
      // recreate Storage-Actor instance and switch state to normal receiver-behaviour:
      storage = context.actorOf(Props[Storage], name = "storage")
      context.unbecome()
  }

  def serviceUnavailable: Receive = {
    case reach: ComputeReach =>
      sender() ! ServiceUnavailable
    case ResumeService =>
      context.unbecome()
  }

}

object StatisticsProvider {
  def props = Props[StatisticsProvider]

  case object ServiceUnavailable
  case object ReviveStorage
  case object ResumeService
}
