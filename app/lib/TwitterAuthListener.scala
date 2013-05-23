package lib

import twitter4j.auth.AccessToken
import twitter4j.{FilterQuery, TwitterStreamFactory, TwitterFactory}
import play.api.{Play, Logger}

object TwitterAuthListener {
  /** Twitter won't let you stream Tweets from more than 5000 users */
  val MaxFollowUsers = 5000

  lazy val screenNameToFollow: String = Play.current.configuration.getString("twitter.sn_to_follow").getOrElse({
    val errorMessage = "You must set twitter.sn_to_follow in the configuration file!"

    Logger.error(errorMessage)

    throw new RuntimeException(errorMessage)
  })

  def onAuthenticate(accessToken: AccessToken) {
    val twitter = TwitterFactory.getSingleton

    if (!twitter.getAuthorization.isEnabled) {
      Logger.error("Not authorized against Twitter but trying to set up stream listener!")

      return // throw error instead once figured out what is happening
    }

    Logger.info("Loading followers IDs for %s".format(screenNameToFollow))

    // we don't need to do any pagination and polling here as it should return 5000, which is the max we can listen to
    // anyway
    val userIDs = twitter.getFollowersIDs(screenNameToFollow, -1).getIDs

    Logger.info("Loaded %d followers".format(userIDs.length))

    // set up the stream listener
    val factory = TwitterStreamFactory.getSingleton

    factory.setOAuthAccessToken(accessToken)

    Logger.info("Setting up stream listener ...")

    factory.addListener(new GuardianTweetListener())

    factory.filter(new FilterQuery(userIDs))

    Logger.info("Listening to stream")
  }
}
