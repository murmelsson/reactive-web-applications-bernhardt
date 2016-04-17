package services

import scala.concurrent.Future

// Define services via some trait, as this makes swapping implementations easier e.g. for testing:
trait RandomNumberService {
  def generateRandomNumber: Future[Int]
}

class DiceDrivenRandomNumberService(dice: DiceService)
extends RandomNumberService {
  override def generateRandomNumber: Future[Int] = dice.throwDice
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
