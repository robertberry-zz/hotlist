package actors

import akka.actor.{ActorRef, Props, Actor}
import twitter4j.auth.AccessToken
import actors.FollowersListActor.{Finished, Progress}
import scala.concurrent.duration._
import scala.util.Random
import akka.event.Logging

object TwitterLifecycleActor {
  val MaximumNumberUsers = 5000

  val ReconnectInterval = 5.minutes

  val RefreshFollowersInterval = 1.day

  sealed trait Message

  case class SetAccessToken(accessToken: AccessToken) extends Message
  case object RefreshFollowers extends Message
  case object Reconnect extends Message
}

class TwitterLifecycleActor extends Actor {
  import context._
  import TwitterLifecycleActor._

  val logger = Logging(system, this)

  var followersListActor: Option[ActorRef] = None
  var tweetStreamActor = actorOf(Props[TweetStreamActor])

  var userIds: Set[Long] = Set.empty

  def randomSelectionOfUsers = Random.shuffle(userIds) take MaximumNumberUsers

  def initializeFollowersListActor() {
    followersListActor = Some(actorOf(Props[FollowersListActor]))
    followersListActor foreach { _ ! FollowersListActor.Start }
  }

  def receive = {
    case SetAccessToken(token) => {
      logger.info(s"Got access token: ${token.getToken} / ${token.getTokenSecret}")
      tweetStreamActor ! TweetStreamActor.SetAccessToken(token)
    }

    case RefreshFollowers => {
      followersListActor match {
        case Some(actor) if actor.isTerminated => {
          logger.error("Not properly cleaning up followers list actor")
          initializeFollowersListActor()
        }
        case Some(actor) => {
          logger.warning("Asked to refresh followers when mid-refresh")
        }
        case None => {
          initializeFollowersListActor()
        }
      }
    }

    case Progress(newUserIds) => {
      if (userIds.isEmpty) {
        system.scheduler.schedule(0 seconds, ReconnectInterval) { self ! Reconnect }
      }

      userIds ++= newUserIds.toSet
    }

    case Reconnect => tweetStreamActor ! TweetStreamActor.Follow(randomSelectionOfUsers.toList)

    case Finished => followersListActor = None; system.scheduler.scheduleOnce(RefreshFollowersInterval) {
      self ! RefreshFollowers
    }
  }
}
