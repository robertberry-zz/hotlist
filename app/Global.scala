import play.api._
import twitter4j.TwitterFactory

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Starting application")

    val twitter = TwitterFactory.getSingleton

    for {
      key <- Play.current.configuration.getString("twitter.oauth.key")
      secret <- Play.current.configuration.getString("twitter.oauth.secret")
    } {
      Logger.info("Setting Twitter key and secret from configuration")
      twitter.setOAuthConsumer(key, secret)
    }


  }

  override def onStop(app: Application) {
    Logger.info("Stopping application")
  }
}
