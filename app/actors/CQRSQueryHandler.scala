package actors

import akka.actor.{Actor, Props}
import helpers.Database
import generated.Tables._
import org.jooq.impl.DSL._
import org.jooq.util.postgres.PostgresDataType
import akka.pattern.pipe

import scala.concurrent.Future
import scala.util.control.NonFatal

class CQRSQueryHandler(database: Database) extends Actor {

  implicit val ec = context.dispatcher

  override def receive = {
    case MentionsToday(phoneNumber) =>
      countMentions(phoneNumber).map { count => 
        DailyMentionsCount(count)
      } recover { case NonFatal(t) =>
        QueryFailed
      } pipeTo sender()  //i.e. pipe to the SMSHandler
  }

  def countMentions(phoneNumber: String): Future[Int] = 
    database.query { sql =>
      sql.selectCount().from(MENTIONS).where(
        MENTIONS.CREATED_ON.greaterOrEqual(currentDate().cast(PostgresDataType.TIMESTAMP)
        )
        .and(MENTIONS.USER_ID.equal(
          sql.select(TWITTER_USER.ID)
            .from(TWITTER_USER)
            .where(TWITTER_USER.PHONE_NUMBER.equal(phoneNumber)))
          )
      ).fetchOne().value1()
    }//query-sql
}
object CQRSQueryHandler {
  def props(database: Database) = Props(classOf[CQRSQueryHandler], database)
}

/** as elmanu says: The resulting query will have the same semantics as the following native PostgreSQL
query:
select count(*)
from mentions
where created_on >= now()::date
and user_id = (select id from twitter_user where phone_number = '1')
*/
