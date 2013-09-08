package actors

import akka.actor.{ActorRef, Actor}
import twitter4j.{TwitterException, TwitterFactory}
import conf.Config
import scala.concurrent.duration._
import lib.TwitterRateLimitError
import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.Logging

object FollowersListActor {
  type TwitterUserId = Long

  type Cursor = Long

  val NoCursor: Cursor = -1

  sealed trait InMessage

  case object Start extends InMessage
  case class GetPage(cursor: Cursor) extends InMessage

  sealed trait OutMessage

  case class Progress(twitterIds: List[TwitterUserId]) extends OutMessage
  case object Finished extends OutMessage
}

class FollowersListActor extends Actor {
  import FollowersListActor._
  import context.become

  val log = Logging(context.system, this)

  private val twitter = TwitterFactory.getSingleton

  private var subscriber: Option[ActorRef] = None

  private var followers: List[TwitterUserId] = Nil

  private def getPage(cursor: Cursor) {
    try {
      log.debug(s"Retrieving page of Twitter followers for cursor $cursor")

      val response = twitter.getFollowersIDs(Config.screenNameToFollow, -1)

      val ids = response.getIDs.toList

      log.info(s"Got page of ${ids.length} followers")

      followers ++= ids

      subscriber foreach { _ ! Progress(followers) }

      if (response.hasNext) {
        self ! GetPage(response.getNextCursor)
      } else {
        subscriber foreach { _ ! Finished }
        context.stop(self)
      }
    } catch {
      case TwitterRateLimitError(e) => {
        val rateLimitStatus = e.getRateLimitStatus

        log.info(s"Exceeded rate limitation while obtaining list of followers. Error:\n${e.getErrorMessage}\n" +
          s"Retrying after ${rateLimitStatus.getSecondsUntilReset} seconds.")

        context.system.scheduler.scheduleOnce(rateLimitStatus.getSecondsUntilReset seconds) {
          self ! GetPage(cursor)
        }
      }

      case e: TwitterException => {
        log.warning(s"Twitter ${e.getErrorCode} error encountered while obtaining list of " +
          s"followers: ${e.getErrorMessage}")

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