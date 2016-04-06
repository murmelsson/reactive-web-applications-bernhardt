package actors

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, Props}
import generated.Tables
import helpers.Database
import generated.Tables._
import generated.tables.records._
import org.jooq.impl.DSL._

class CQRSEventHandler(database: Database) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    // Subscribe to all messages matching the actors.Event trait and deliver said messages to ourselves:
    context.system.eventStream.subscribe(self, classOf[Event])
  }

  def receive = {
    case UserRegistered(phoneNumber, userName, timestamp) =>
      log.info("In CQRS userReqd-received: " + phoneNumber + " " + userName)
      database.withTransaction { sql =>
        sql.insertInto(TWITTER_USER)
          .columns(TWITTER_USER.CREATED_ON, TWITTER_USER.PHONE_NUMBER, TWITTER_USER.TWITTER_USER_NAME)
          .values(new Timestamp(timestamp.getMillis), phoneNumber, userName)
          .execute()
      }

    case ClientEvent(phoneNumber, userName, MentionsSubscribed(timestamp), _) =>
      log.info("In CQRS ClientEvent-received MentionsSubscribed")
      database.withTransaction { sql =>
        sql.insertInto(MENTION_SUBSCRIPTIONS)
          .columns(
            MENTION_SUBSCRIPTIONS.USER_ID,
            MENTION_SUBSCRIPTIONS.CREATED_ON
          )
          .select(
            select(      //select-method provided by wildcard import of DSL-class
              TWITTER_USER.ID,
              value(new Timestamp(timestamp.getMillis))  //timestamp insert as const value using value-method.
            )
            .from(TWITTER_USER)
            .where(
              TWITTER_USER.PHONE_NUMBER.equal(phoneNumber)
              .and(
                TWITTER_USER.TWITTER_USER_NAME.equal(userName)
              )
            )
          ).execute()
       }  

    case ClientEvent(phoneNumber, userName, 
                     MentionReceived(id, created_on, from, text, timestamp), _) =>
      log.info("In CQRS ClientEvent-received MentionReceived")
      database.withTransaction { sql =>
        sql.insertInto(MENTIONS)
          .columns(
            MENTIONS.USER_ID,
            MENTIONS.CREATED_ON,
            MENTIONS.TWEET_ID,
            MENTIONS.AUTHOR_USER_NAME,
            MENTIONS.TEXT
          )
          .select(
             select(
               TWITTER_USER.ID,
               value(new Timestamp(timestamp.getMillis)),
               value(id),
               value(from),
               value(text)
             )
             .from(TWITTER_USER)
             .where(
               TWITTER_USER.PHONE_NUMBER.equal(phoneNumber)
               .and(
                 TWITTER_USER.TWITTER_USER_NAME.equal(userName)
               )
             )
          ).execute()
      }
  }//receive
}

object CQRSEventHandler {
  def props(database: Database) = Props(classOf[CQRSEventHandler], database)
}