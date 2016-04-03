package actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp._

//murm: may need to use DI to provide the ActorRef?
class SMSHandler(connection: ActorRef) extends Actor with ActorLogging {

  def receive = {

    case Received(data) =>
      log.info(s"Received message: ${data.utf8String}")
      // Print out received data (which was encoded as ByteString) by assuming it is a UTF-8 string
      connection ! Write(data)

    case PeerClosed =>      //Handle client-disconnect
      context stop self
  }//receive
}
