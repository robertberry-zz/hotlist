package lib

import twitter4j.auth.AccessToken
import twitter4j.TwitterFactory
import akka.actor.{Props, ActorRef}
import actors.{TwitterLifecycleActor, Actors}
import grizzled.slf4j.Logging

object TwitterAuthListener extends Logging {
  var lifecycleManagementActor: Option[ActorRef] = None

  def onAuthenticate(accessToken: AccessToken) {
    val twitter = TwitterFactory.getSingleton

    if (!twitter.getAuthorization.isEnabled) {
      logger.error("Not authorized against Twitter but trying to set up stream listener!")

      return // throw error instead once figured out what is happening
    }

    logger.info("Starting lifecycle manager")

    lifecycleManagementActor = Some(Actors.system.actorOf(Props[TwitterLifecycleActor]))

    lifecycleManagementActor foreach { actor =>
      actor ! TwitterLifecycleActor.SetAccessToken(accessToken)
      actor ! TwitterLifecycleActor.RefreshFollowers
    }
  }
}
