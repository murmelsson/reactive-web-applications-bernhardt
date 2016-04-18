package services

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits._

// Define services via some trait, as this makes swapping implementations easier e.g. for testing:
trait RandomNumberService {
  def generateRandomNumber: Future[Int]
}

class DiceDrivenRandomNumberService(dice: DiceService)
extends RandomNumberService {
  //override def generateRandomNumber: Future[Int] = dice.throwDice
  // Replace first implementation of generateRandomNumber with a more resilient version: 
  var tries = 1
  override def generateRandomNumber: Future[Int] = dice.throwDice.recoverWith {
    case NonFatal(t) if tries < 5 => 
      tries += 1             //so we allow 5 tries in total
      generateRandomNumber   //i.e. retry itself
  }
}

trait DiceService {
  def throwDice: Future[Int]
}

class RollingDiceService extends DiceService {
  override def throwDice: Future[Int] = 
    Future.successful {
      4  //"chosen by fair dice roll, guaranteed to be random"
         //(uhhh...not!)
    }
}
