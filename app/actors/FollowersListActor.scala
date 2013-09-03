package actors

import akka.actor.{ActorRef, Actor}
import twitter4j.{TwitterException, TwitterFactory}
import conf.Config
import grizzled.slf4j.Logging
import scala.concurrent.duration._
import lib.TwitterRateLimitError
import scala.concurrent.ExecutionContext.Implicits.global

object FollowersListActor {
  type TwitterUserId = Long

  type Cursor = Long

  val NoCursor: Cursor = -1

  sealed trait InMessage

  case object Start extends InMessage
  case class GetPage(cursor: Cursor) extends InMessage

  sealed trait OutMessage

  case class Finished(twitterIds: List[TwitterUserId]) extends OutMessage
}

class FollowersListActor extends Actor with Logging {
  import FollowersListActor._
  import context.become

  private val twitter = TwitterFactory.getSingleton

  private var subscriber: Option[ActorRef] = None

  private var followers: List[TwitterUserId] = Nil

  private def getPage(cursor: Cursor) {
    try {
      debug(s"Retrieving page of Twitter followers for cursor $cursor")

      val response = twitter.getFollowersIDs(Config.screenNameToFollow, -1)

      val ids = response.getIDs.toList

      info(s"Got page of ${ids.length} followers")

      followers ++= ids

      if (response.hasNext) {
        self ! GetPage(response.getNextCursor)
      } else {
        subscriber foreach { _ ! Finished(followers) }
        context.stop(self)
      }
    } catch {
      case TwitterRateLimitError(e) => {
        info(s"Exceeded rate limitation while obtaining list of followers. Retrying after ${e.getRetryAfter} seconds")

        context.system.scheduler.scheduleOnce(e.getRetryAfter seconds) {
          self ! GetPage(cursor)
        }
      }

      case e: TwitterException => {
        warn(s"Twitter ${e.getErrorCode} error encountered while obtaining list of followers: ${e.getErrorMessage}")

        // think about how to recover from this - maybe need a supervisor actor to take account of it
        throw e
      }
    }
  }

  def receive = {
    case Start => {
      subscriber = Some(sender)
      become(load)
      self ! GetPage(NoCursor)
    }
  }

  def load: Receive = {
    case GetPage(cursor) => getPage(cursor)
  }
}