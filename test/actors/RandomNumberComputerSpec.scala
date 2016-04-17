package actors

import akka.actor.ActorSystem  //or akka.actors.ActorSystem?
import com.typesafe.config.ConfigFactory
import akka.testkit._
import scala.concurrent.duration._
import org.scalatest._
import actors.RandomNumberComputer._

class RandomNumberComputerSpec(_system: ActorSystem)
extends TestKit(_system)  //provides testing-functionality for Actors.
with ImplicitSender       //sets TestKit's testActor as target(-receiver) of sent messages by declaring implicit ActorRef pointing
                          //to testActor.
with FlatSpecLike         //mixes in FlatSpec-behaviour.
with ShouldMatchers    
with BeforeAndAfterAll {  //support for executing custom-code before and after all cases

  //Define a default constructor which provides an ActorSystem:
  //def this() = this(ActorSystem("RandomNumberComputerSpec"))
  //or define the default-constructor to search for a scaling factor
  //from getenv:
  def this() = this(
    ActorSystem(
      "RandomNumberComputerSpec",
      ConfigFactory.parseString(
        s"akka.test.timefactor=" +
          sys.props.getOrElse("SCALING_FACTOR", default = "1.0")
      )
    )
  )

  override def afterAll {
    //shutdown the ActorSystem after all cases have run:
    TestKit.shutdownActorSystem(system)
  }

  "A RandomNumberComputerSpec" should "send back a random number" in {
    //Initialise the Actor to test:
    val randomNumberComputer = system.actorOf(RandomNumberComputer.props)

    //TestKit's within-method checks whether we get a result within
    //the specified timeframe, with timescaling taken into account:
    within(100.millis.dilated) {
      randomNumberComputer ! ComputeRandomNumber(100)

      //we expect testActor to receive a RandomNumber, though we don't know which number
      //we will get... because it's, uhh, random (at least pseudo-random):
      expectMsgType[RandomNumber]
    }
  }
}
