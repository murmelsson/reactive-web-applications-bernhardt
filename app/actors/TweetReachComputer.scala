package actors

//import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.actor._
import akka.pattern.pipe
import messages._
import play.api.libs.json.JsArray
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WS
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.control.NonFatal
import play.api.Play.current

class TweetReachComputer(userFollowersCounter: ActorRef, storage: ActorRef) //1 ActorRefs as constructor parameters
extends Actor with ActorLogging with TwitterCredentials {

  implicit val executionContext = context.dispatcher   //2 use this.Actor's dispatcher as ec for executing Futures
import scala.concurrent.duration._
  var followerCountsByRetweet = Map.empty[FetchedRetweet, List[FollowerCount]]  //3 cache for currently computed follower-counts for a retweet

  // Reminder of unacknowledged messages every 20 seconds:
  val retryScheduler: Cancellable = context.system.scheduler.schedule(
                                      1.second, 20.seconds, self, ResendUnacknowledged)

  override def postStop(): Unit = {
    retryScheduler.cancel()
  }

  def receive = {

    //val originalSender: ActorRef = sender() //in case of Future-failure, capture original sender (as sender may change in the meantime)
    //val originalSender = sender() //does not compile, but compiles inside a matching-block.

    case ComputeReach(tweetId) =>   //using pipe we avoid concurrent operations issue (race conditions):
    //fetchRetweets(tweetId, sender()) pipeTo self   //4b - Pipe the fetchRetweets Future to this actor itself (original version without recover-path)
    val originalSender = sender()
    fetchRetweets(tweetId, originalSender).recover {
      case NonFatal(t) =>
        RetweetFetchingFailed(tweetId, t, originalSender)
     } pipeTo self

    case fetchedRetweets: FetchedRetweet =>   //5b - Handle the received result of the Future
      followerCountsByRetweet += fetchedRetweets -> List.empty
      fetchedRetweets.retweeters.foreach { rt =>
        userFollowersCounter ! FetchFollowerCount(fetchedRetweets.tweetId, rt)
      }

    case count @ FollowerCount(tweetId, _, _) =>
    log.info("received followers-count for tweet {}", tweetId)
    fetchedRetweetsFor(tweetId).foreach { fetchedRetweets =>
      updateFollowersCount(tweetId, fetchedRetweets, count)
    }

    case ReachStored(tweetId) =>
    followerCountsByRetweet.keys
      .find(_.tweetId == tweetId)
      .foreach { key =>
        followerCountsByRetweet = followerCountsByRetweet.filterNot(_._1 == key)  //6 remove state after score has been stored
      }

    case ResendUnacknowledged =>
      val unacknowledged = followerCountsByRetweet.filterNot {
        case (retweet, counts) =>
          retweet.retweeters.size != counts.size  //only consider cases where counting-up !match
      }
      unacknowledged.foreach { case (retweet, counts) =>
        // send new StoreReach msg to Storage Actor:
        val score = counts.map(_.followersCount).sum
        storage ! StoreReach(retweet.tweetId, score)
      } 
 }

  
  case object ResendUnacknowledged

  case class FetchedRetweet(tweetId: BigInt, retweeters: List[BigInt], client: ActorRef)

  case class RetweetFetchingFailed(tweetId: BigInt, cause: Throwable, client: ActorRef) //holds context of failure


  def fetchedRetweetsFor(tweetId: BigInt) = followerCountsByRetweet.keys.find(_.tweetId == tweetId)

  def updateFollowersCount(tweetId: BigInt, fetchedRetweets: FetchedRetweet, count: FollowerCount) = {
    val existingCounts = followerCountsByRetweet(fetchedRetweets)
    followerCountsByRetweet = followerCountsByRetweet.updated(fetchedRetweets, count :: existingCounts) //7 update state of retrieved follower-counts
    val newCounts = followerCountsByRetweet(fetchedRetweets)

    if (newCounts.length == fetchedRetweets.retweeters.length) {  //8 check if all follower counts were retrieved
      log.info("Received all retweeters' followers-count for tweet {}, computing sum", tweetId)
      val score = newCounts.map(_.followersCount).sum

      fetchedRetweets.client ! TweetReach(tweetId, score)    //9 reply(tell) client the final score for tweet
      storage ! StoreReach(tweetId, score)      //10 ask(tell) for tweet-score to be persisted
    }
  }

  def fetchRetweets(tweetId: BigInt, client: ActorRef): Future[FetchedRetweet] = {    //11 fetch retweets TODO by using WS calls to Twitter API
  // https://github.com/manuelbernhardt/reactive-web-applications/blob/master/CH06/app/actors/TweetReachComputer.scala

    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/statuses/retweeters/ids.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("id" -> tweetId.toString)
          .withQueryString("stringify_ids" -> "true")
          .get().map { response =>
          if (response.status == 200) {
            val ids = (response.json \ "ids").as[JsArray].value.map(v => BigInt(v.as[String])).toList
            FetchedRetweet(tweetId, ids, client)
          } else {
            throw new RuntimeException(s"Could not retrieve details for Tweet $tweetId")
          }
        }
    }.getOrElse {
      Future.failed(new RuntimeException("You did not correctly configure the Twitter credentials"))
    }
  }

}


object TweetReachComputer {
  def props(followersCounter: ActorRef, storage: ActorRef) = 
    Props(classOf[TweetReachComputer], followersCounter, storage)
}
