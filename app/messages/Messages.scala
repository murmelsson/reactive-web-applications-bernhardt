package messages

case class ComputeReach(tweetId: BigInt)
case class TweetReach(tweetId: BigInt, score: Int)

case class FetchFollowerCount(tweetId: BigInt, userId: BigInt)
case class FollowerCount(tweetId: BigInt, userId: BigInt, followersCount: Int)

case class StoreReach(tweetId: BigInt, score: Int)
case class ReachStored(tweetId: BigInt)

case object TweetReachCouldNotBeComputed
