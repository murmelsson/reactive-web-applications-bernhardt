package actors

import actors.RandomNumberFetcher._
import akka.actor.{Props, Actor}
import play.api.libs.json.{JsArray, Json, JsResultException}
import play.api.libs.ws.WSClient
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.{pipe, CircuitBreaker, CircuitBreakerOpenException}
import scala.util.Random
import scala.util.control.NonFatal

class RandomNumberFetcher(ws: WSClient) extends Actor {
  implicit val ec = context.dispatcher

  //our circuit-breaker:
  val breaker = CircuitBreaker(
    scheduler = context.system.scheduler,
    maxFailures = 3,
    callTimeout = 3.seconds,
    resetTimeout = 20.seconds
  )

  def receive = {
    case FetchRandomNumber(max) =>
      //Result of Future-call to random.org gets piped to sender(i.e. through to the requester of a random number):
      //fetchRandomNumber(max).map(RandomNumber) pipeTo sender()
      val safeCall = breaker.withCircuitBreaker(
        fetchRandomNumber(max).map(RandomNumber)
      )
      safeCall recover {
        case js: JsResultException => computeRandomNumber(max)
        case o: CircuitBreakerOpenException => computeRandomNumber(max)
      } pipeTo sender()
  }

  //useful fallback compute-method done "backend-locally" without needing call to the random.org API:
  def computeRandomNumber(max: Int) = RandomNumber(Random.nextInt(max))

  def fetchRandomNumber(max: Int): Future[Int] = 
    ws
      .url("https://api.random.org/json-rpc/1/invoke")
      .post(Json.obj(
        "jsonrpc" -> "2.0",
        "method" -> "generateIntegers",
        "params" -> Json.obj(
          //replaced real apiKey with dummy value before github-push:
          "apiKey" -> "8000okto-....-naut-it00-asemiiiinnnn",
          "n" -> 1,    //one random integer will do us fine
          "min" -> 0,
          "max" -> max, //should be in the range of allowed values  [-1e9,1e9] 
          "replacement" -> true, //random duplication allowed
          "base" -> 10
        ),
        "id" -> 42      //"A request identifier that allows the client to match responses to request. The service will return this unchanged in its response."(https://api.random.org/json-rpc/1/introduction)
      )).map { response =>
        (response.json \ "result" \ "random" \ "data")
          .as[JsArray]
          .value
          .head
          .as[Int]  
      }//mapping-ends-method-fetch-rnd-num

}


object RandomNumberFetcher {
  def props(ws: WSClient) = Props(classOf[RandomNumberFetcher], ws)
  case class FetchRandomNumber(max: Int)
  case class RandomNumber(n: Int)
}

