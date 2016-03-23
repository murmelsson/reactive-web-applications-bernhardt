package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.Json

class TwitterStreamer(out: ActorRef) extends Actor {
  def receive = {
    case "subscribe" =>
      Logger.info("A client subscribed to the Twitter-Stream")
      out ! Json.obj("text" -> "Hello, world!")
  }
}

// Companion object constructs a new TwitterStreamer using the client's ActorRef,
// and returns it wrapped in its Props (which is kind-of the metadata of an Actor)
object TwitterStreamer {
  def props(out: ActorRef) = Props(new TwitterStreamer(out))
}
