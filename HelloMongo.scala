
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Future

import scala.concurrent.duration._

/** Here we are assuming that 
(a) some data was populated into a MongoDB, in this case the db is called "persons", and (confusingly)
the collections in that db are called "city" and "persons", as after running the curl posts in README of this
Activator template (working) project: https://github.com/jonasanso/play-reactive-mongo-db
(b) // a mongod process running on localhost, so we can connect to it. // to check it is all functioning, use (on Ubuntu) :~$: tail var/log/mongodb/mongod.log
(unless you have moved your data and log locations, in which case you can change the default path to see logs).

Useful links:
http://reactivemongo.org/
https://docs.mongodb.org/manual/
https://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/   (for ubuntu, obviously)
// This didn't test insert.collection from code, instead just read curl-added data
*/
object HelloMongo extends App{ 
  //def main(args: Array[String]) = { 
    println("Hi! Work-in-Progress... first find osaka in db collection'city': ") 

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new reactivemongo.api.MongoDriver
  val connection = driver.connection(List("localhost"))

  // Gets a reference to the database "plugin"
  val db = connection("persons")
  
  val collection = db("city")

  // Try running db.city.find(name="osaka") :
  val query = BSONDocument("name" -> "osaka")
    collection.
    find(query).
    cursor[BSONDocument].
    //enumerator(Iteratee.foreach { doc =>
    enumerate().apply(Iteratee.foreach { doc =>
      println(s"found city-document: ${BSONDocument pretty doc}")
    })

  println("Next find some person in db.persons, using a Future: ") 
  val coll2 = db("persons")
  val query2 = BSONDocument("name" -> "oliver")
// Or, the same with getting a list
  val futureList: Future[List[BSONDocument]] =
    coll2.
      find(query2).
      cursor[BSONDocument].
      collect[List]()

  futureList.map { list =>
    list.foreach { doc =>
      println(s"found persons-document: ${BSONDocument pretty doc}")
    }
  }

  // Good practice to close connections to a DB But doing this we close the cursor before it gets chance to iteratee the retrieved data.
  // Not closing seems to leave a standalone console program running, but never mind.
  // This is somehow to do with how to know that the ActorSystem used by the reactivemongo MongoDriver has finished it's work.
  // But no point in this smoke-test to work out some solution for that.
  //val threeSeconds = FiniteDuration(3, "seconds")
  //connection.askClose()(threeSeconds)
  //driver.close(FiniteDuration(4, "seconds"))  // also close the driver, so that the ActorSystem it has spun up goes away (if not done, program will still be running).

  println("Done.") 


  //}

}


/** Here is the build.sbt for this to work (in project root):
name := """reactive-mongo-db-poc"""

version := "1.0-SNAPSHOT"

//lazy val root = project in file(".")

scalaVersion := "2.11.7"

// routesGenerator := InjectedRoutesGenerator

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.11.10",
  "org.slf4j" % "slf4j-simple" % "1.6.4"
)

scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation")
*/

/** and here is the application.conf (in this case under standard sbt project path: src/main/resources/ ). If you don't include slf4j and a log-level conf, may result in compile failure.:
 
mongo-async-driver {
  akka {
    loglevel = WARNING
  }
}

*/
