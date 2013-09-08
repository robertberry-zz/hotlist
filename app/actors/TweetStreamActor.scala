package actors

import akka.actor.Actor
import twitter4j.{TwitterFactory, TwitterStream, FilterQuery, TwitterStreamFactory}
import twitter4j.auth.AccessToken
import lib.GuardianTweetListener
import akka.event.Logging
import twitter4j.conf.Configuration

object TweetStreamActor {
  sealed trait Message

  case class SetAccessToken(accessToken: AccessToken)
  case class Follow(userIDs: List[Long]) extends Message
}

class TweetStreamActor extends Actor {
  import context._
  import TweetStreamActor._

  val log = Logging(system, this)

  var accessToken: Option[AccessToken] = None

  var currentStream: Option[TwitterStream] = None

  val twitter = TwitterFactory.getSingleton

  def receive = {
    case SetAccessToken(token) => {
      log.info(s"Got access token ${token.getToken} / ${token.getTokenSecret}")

      accessToken = Some(token)
    }

    case Follow(userIDs) => accessToken match {
      case Some(token) => {
        currentStream foreach { stream =>
          log.info("Shutting down stream")
          stream.shutdown()
        }

        val stream = new TwitterStreamFactory().getInstance(twitter.getAuthorization)
        stream.addListener(new GuardianTweetListener)

        log.info(s"Spinning up stream for ${userIDs.length} users")

        stream.filter(new FilterQuery(userIDs.toArray))

        currentStream = Some(stream)
      }

      case None => log.warning("Tweet stream actor received message to follow without having previously received " +
        "message to set access token")
    }
  }
}
