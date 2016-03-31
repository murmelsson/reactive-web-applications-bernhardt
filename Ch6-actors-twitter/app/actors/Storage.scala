package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import reactivemongo.api.{MongoDriver, MongoConnection}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONBinary, Subtype, BSONDateTime}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.core.errors.ConnectionException
import org.joda.time.DateTime
import akka.pattern.pipe
import messages.{ReachStored, StoreReach}
import actors.Storage._

/**Remember we need in build.sbt:
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.10"
)*/

// We need a case class with companion-object to handle preparation of BSONDocument for persisting to Mongo:
case class StoredReach(when: DateTime, tweetId: BigInt, score: Int)

object StoredReach {
  implicit object BigIntHandler extends BSONDocumentReader[BigInt] with BSONDocumentWriter[BigInt] {
    def write(bigInt: BigInt): BSONDocument = BSONDocument(
      "signum" -> bigInt.signum,
      "value" -> BSONBinary(bigInt.toByteArray, Subtype.UserDefinedSubtype))

    def read(doc: BSONDocument): BigInt = BigInt(
      doc.getAs[Int]("signum").get, {
        val buf = doc.getAs[BSONBinary]("value").get.value
        buf.readArray(buf.readable())
        }
    )  //returns a BigInt constructed from signum and binary-value
  }

  implicit object StoredReachHandler extends BSONDocumentReader[StoredReach] with BSONDocumentWriter[StoredReach] {
    override def read(bson: BSONDocument): StoredReach = {
      val when = bson.getAs[BSONDateTime]("when").map(t => new DateTime(t.value)).get
      val tweetId = bson.getAs[BigInt]("tweet_id").get
      val score = bson.getAs[Int]("score").get
      StoredReach(when, tweetId, score)  //returns a StoredReach from the BSONDocument
    }

    override def write(r: StoredReach): BSONDocument = BSONDocument(
      "when" -> BSONDateTime(r.when.getMillis),
      "tweetId" -> r.tweetId,
      "tweet_id" -> r.tweetId,
      "score" -> r.score
    )  //returns a BSONDocument from a StoredReach
  }

}


class Storage extends Actor with ActorLogging {

//  override def preStart(): Unit = log.info("hello, world (stor).")
//
//  def receive = {
//    case message =>   // do nothing
//  }

  val Database = "twitterService"
  val ReachCollection = "ComputedReach"

  implicit val executionContext = context.dispatcher  //do we need to import akka.actor.ActorContext?

  val driver: MongoDriver = new MongoDriver    // returns instance of ReactiveMongo db-driver
  // Declare MongoConnection as a State of this Actor:
  var connection: MongoConnection = driver.connection(List("localhost"))  // assume mongod process running. //bash$: tail var/log/mongodb/mongod.log  //Remember not to share state outside of the Actor!
  var db = connection.db(Database)      // where/when is this db created? maybe at insert-time in the code?
  var collection: BSONCollection = db.collection[BSONCollection](ReachCollection)  //where/when is this collection created?
  
  // Reinitialise the connection if needed after the Actor has already (re-/)started,
  // (in case the restart-reason is a ConnectionException):
  override def postRestart(reason: Throwable): Unit = {
    reason match {
      case ce: ConnectionException =>
      // try to get a new connection:
        connection = driver.connection(List("localhost"))
        db = connection.db(Database)
        collection = db.collection[BSONCollection](ReachCollection)
    }
    super.postRestart(reason)
  }

  // Close connection and driver once this Actor has stopped (note that the same tactic doesn't work easily with a standalone app, where we have less control over the implicit ActorSystem used by reactivemongo API):
  override def postStop(): Unit = {
    connection.close()
    driver.close()
  }

  var currentWrites = Set.empty[BigInt]

  def receive = {
    case StoreReach(tweetId, score) =>  
      log.info("Storing reach for tweet: {}", tweetId)
      if (!currentWrites.contains(tweetId)) {
        currentWrites = currentWrites + tweetId
        val originalSender = sender()
        collection.insert(StoredReach(DateTime.now, tweetId, score)).map { lastError =>
          LastStorageError(lastError, tweetId, originalSender)
          }.recover {
            case _ => 
            log.info("Trying to recover from collection.insert (case StoreReach(tweetId: {}, score) problem...", tweetId)
            currentWrites = currentWrites.filterNot( _ == tweetId )  //remove tweetId due to failure-case
        } pipeTo self
      }
    case LastStorageError(error, tweetId, client) =>
      if(error.inError) {
        log.info("Error in storage-attempt, WriteResult.inError: {}", tweetId)
        currentWrites = currentWrites.filterNot( _ == tweetId )  //remove tweetId due to failure-case
      } else {
        client ! ReachStored(tweetId)
      }
    
  }

}

object Storage {
  case class LastStorageError(result: WriteResult, tweetId: BigInt, client: ActorRef)
}



/** libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.10",
  "org.slf4j" % "slf4j-simple" % "1.6.4" //(maybe not needed for playfr)
)

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation")
*/

/** and here is the application.conf for mongo-async-driver (in this case under standard sbt project path: src/main/resources/ ). If you don't include slf4j and a log-level conf, may result in compile failure.:
 
mongo-async-driver {
  akka {
    loglevel = WARNING
  }
}

//or if using the activator template play-reactive-mongo-db, conf/application.conf looks like:
mongodb.uri = "mongodb://localhost/persons"

play.modules.enabled += "play.modules.reactivemongo.ReactiveMongoModule"

mongo-async-driver {
  akka {
    loglevel = WARNING
  }
}

//while a class using the db is defined e.g. like this:
class CityController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  val cities: JSONCollection = db.collection[JSONCollection]("city")

  def create(name: String, population: Int) = Action.async {
    cities.insert(City(name, population)).map(lastError =>
      Ok("Mongo LastError: %s".format(lastError)))
  }
//...

*/

