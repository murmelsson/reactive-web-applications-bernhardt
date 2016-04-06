package actors

import org.joda.time.DateTime

trait Command {
  val phoneNumber: String
}

trait Event {
  val timestamp: DateTime
}

case class RegisterUser(phoneNumber: String, userName: String) extends Command
case class UserRegistered(phoneNumber: String, userName: String,
                          timestamp: DateTime = DateTime.now) extends Event
case class ConnectUser(phoneNumber: String) extends Command

case class SubscribeMentions(phoneNumber: String) extends Command

case class MentionReceived(id: String, created_on: DateTime, from: String, text: String,
                           timestamp: DateTime = DateTime.now) extends Event
case class MentionsSubscribed(timestamp: DateTime = DateTime.now) extends Event
case class AcknowledgeMention(id: String) // this message is sent back directly without phone number, so we don't extend our Command here
case class MentionAcknowledged(id: String, timestamp: DateTime = DateTime.now) extends Event

case class ClientEvent(phoneNumber: String, userName: String, event: Event, timestamp: DateTime = DateTime.now) extends Event

case class UnknownUser(phoneNumber: String)

case class InvalidCommand(reason: String)

trait Query
trait QueryResult
case class MentionsToday(phoneNumber: String) extends Query
case class DailyMentionsCount(count: Int) extends QueryResult
case object QueryFailed extends QueryResult

