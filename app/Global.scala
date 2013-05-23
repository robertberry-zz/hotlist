import lib.TwitterAuthListener
import models.TwitterAccessToken
import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api._
import play.api.db.DB
import twitter4j.TwitterFactory
import org.squeryl.PrimitiveTypeMode.inTransaction

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Starting application")

    /** Set OAuth key and secret for Twitter */
    val twitter = TwitterFactory.getSingleton

    for {
      key <- Play.current.configuration.getString("twitter.oauth.key")
      secret <- Play.current.configuration.getString("twitter.oauth.secret")
    } {
      Logger.info("Setting Twitter key and secret from configuration")
      twitter.setOAuthConsumer(key, secret)
    }

    /** DB connection */
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      case Some("org.h2.Driver") => Some(() => getSession(new H2Adapter, app))
      case Some("org.postgresql.Driver") => Some(() => getSession(new PostgreSqlAdapter, app))
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }

    /** Do we already have Twitter authentication? */
    inTransaction {
      for {
        tokenModel <- TwitterAccessToken.last
        token = tokenModel.toToken
      } {
        Logger.info("Loaded Twitter access token from database, authenticating ...")

        twitter.setOAuthAccessToken(token)

        Logger.info("Authenticated against Twitter")

        TwitterAuthListener.onAuthenticate(token)
      }
    }
  }

  def getSession(adapter: DatabaseAdapter, app: Application) =
    Session.create(DB.getConnection()(app), adapter)

  override def onStop(app: Application) {
    Logger.info("Stopping application")
  }
}
