package actors

import java.net.InetSocketAddress              //so we can use sockets
import akka.actor.{Props, ActorLogging, Actor}
import akka.io.Tcp._                           //so we can use tcp/ip
import akka.io.{Tcp, IO}                       //so we can let AkkaIO do the plumbing

class SMSServer extends Actor with ActorLogging {

  import context.system      //the ActorSystem is required by AkkaIO

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6666))

  def receive = {

    case Bound(localAddress) =>      //successfully bound to socket on localhost, port 6666
      log.info(s"SMS-server listening on $localAddress")   //murm: aaltosulut localAddress:ille?

    case CommandFailed(_: Bind) =>   //give up when socket-bind attempt failed
      context stop self

    case Connected(remote, local) => //remote connection exists
      val connection = sender()
      // Set up new SMSHandler for the client connection by creating child SMSHandler
      // and passing the client connection to said handler:
      val handler = context.actorOf(Props(classOf[SMSHandler], connection))

      connection ! Register(handler) //register our handler with AkkaIO subsystem
  }//receive
}
