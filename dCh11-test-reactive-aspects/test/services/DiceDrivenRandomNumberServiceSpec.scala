package services

import java.util.concurrent.atomic.AtomicInteger
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
  }//service-should-return-num

  it should "be able to cope with problematic dice throws" in {
    val overzealousDiceThrowingService = new DiceService {
      //Good old Java5 AtomicInteger for threadsafer counting:
      val counter = new AtomicInteger()
      override def throwDice: Future[Int] = {
        val count = counter.incrementAndGet()
        if (count % 5 == 0) {
          Future.successful(4)
        } else {
          Future.failed(new RuntimeException("The die landed on a vertex and just spun to a vertexy standstill!!"))
        }
      }//throwDice
    }//overzealous-val

    val randomNumberService = new DiceDrivenRandomNumberService(overzealousDiceThrowingService)

    whenReady(randomNumberService.generateRandomNumber) { result =>
      result shouldBe(4)   //which half the time it won't be
    }
  }//service-should-cope-with-future-failure

}
