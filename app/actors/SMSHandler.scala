package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp._
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._

//murm: use DI to provide the ActorRef? No, it's not a startup-wiring-concern
class SMSHandler(connection: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(2.seconds)
  implicit val ec = context.dispatcher

  lazy val commandHandler = context.actorSelection(
    "akka://application/user/sms/commandHandler")

  val MessagePattern = """[\+]([0-9]*) (.*)""".r  //Pattern for matching incoming msgs
  val RegistrationPattern = """register (.*)""".r //Pattern for valid registration-command
  def receive = {
    case Received(data) =>
      log.info(s"Received message: ${data.utf8String}")
      // // Print out received data (which was encoded as ByteString) by assuming it is a UTF-8 string
      // connection ! Write(data)  //just a wee test response
      data.utf8String.trim match {
        case MessagePattern(number, message) =>
          message match {
            case RegistrationPattern(userName) =>
              // Tell command-handler to register the user:
              commandHandler ! RegisterUser(number, userName)
            case "subscribe mentions" =>
              commandHandler ! SubscribeMentions(number)
            case "connect" =>
              commandHandler ! ConnectUser(number)
            case other =>
              log.warning(s"Invalid message: $other")
              sender() ! Write(ByteString("Invalid message format\n"))
          }
      }
      case registered: UserRegistered =>
        connection ! Write(ByteString("Registration successful!\n"))
      case subscribed: MentionsSubscribed =>
        connection ! Write(ByteString("Mentions subscribed\n"))
      case MentionReceived(id, created, from, text, _) =>
        connection ! Write(ByteString(s"mentioned by @$from: $text\n"))
        sender() ! AcknowledgeMention(id)
      case UnknownUser(number) =>
        connection ! Write(ByteString(s"Unknown user $number\n"))
      case InvalidCommand(reason) =>
        connection ! Write(ByteString(reason + "\n"))
      case PeerClosed => //Handle client-disconnect
          context stop self
  }//receive
}
