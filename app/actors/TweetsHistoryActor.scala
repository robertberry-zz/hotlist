package actors

import akka.actor.Actor
import models.Tweet
import com.twitter.util.LruMap
import lib.Time._
import actors.TweetsHistoryActor.{AddToHistory, GetHistoryFor}

object TweetsHistoryActor {
  sealed trait Message

  case class GetHistoryFor(uri: String) extends Message
  case class AddToHistory(uri: String, tweet: Tweet)
}

object TweetsHistory {
  val MaxLength = 20

  val empty = TweetsHistory(Nil)
}

case class TweetsHistory(tweets: List[Tweet]) {
  import TweetsHistory._

  assert(tweets.length <= MaxLength, s"A Tweet History has a maximum length of $MaxLength")

  def add(tweet: Tweet) =
    TweetsHistory((tweet :: tweets).sortBy(_.tweetedAt)(DateTimeOrdering.reverse).take(MaxLength))
}

class TweetsHistoryActor extends Actor {
  val cacheSize = 50000

  val cache = new LruMap[String, TweetsHistory](cacheSize)

  def receive = {
    case GetHistoryFor(uri) => sender ! cache.getOrElse(uri, TweetsHistory.empty)
    case AddToHistory(uri, tweet) => cache += (uri -> cache.getOrElse(uri, TweetsHistory.empty).add(tweet))
  }
}
