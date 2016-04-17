package services

import org.scalatest.time.{Millis, Span}
import org.scalatest.{ShouldMatchers, FlatSpec}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Future

//ScalaFutures trait provides whenReady-method which wraps a Future
//and checks expected result in method-body, and also provides
//a default PatienceConfig object which can be configured.
class DiceDrivenRandomNumberServiceSpec
extends FlatSpec with ScalaFutures with ShouldMatchers {
  "The DiceDrivenRandomNumberService" should
    "return a number provided by a die" in {

    implicit val patienceConfig = 
      PatienceConfig(
        //how much time Future gets to succeed before timeout:
        timeout = scaled(Span(150, Millis)),
        //polling interval between checks for success:
        interval = scaled(Span(15, Millis))
      )

    val diceService = new DiceService {
      override def throwDice: Future[Int] = Future.successful(4)
    }

    val randomNumberService = new DiceDrivenRandomNumberService(diceService)

    //Invoke service-method we want to test as param to whenReady:
    whenReady(randomNumberService.generateRandomNumber) { result =>
      //Check result is correct:
      result shouldBe(4)
    }
  }//service-should
}
