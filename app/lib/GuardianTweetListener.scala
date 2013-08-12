package lib

import twitter4j.{Status, StatusDeletionNotice, StallWarning, StatusListener}
import play.api.Logger
import org.joda.time.DateTime
import ranking.{HotList, StatusUtils}
import scala.concurrent.ExecutionContext.Implicits.global
import actors.{LinkTitleScraperActor, LinkResolverActor, Actors}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class GuardianTweetListener extends StatusListener {
  implicit val urlResolutionTimeout = Timeout(1 second)

  def onStatus(status: Status) {
    Logger.info("Got status: %s says %s".format(status.getUser.getScreenName, status.getText))

    val urls = StatusUtils.extractUrls(status.getText)
    val date = new DateTime(status.getCreatedAt)
    urls foreach { url =>
      Actors.linkResolver ? LinkResolverActor.Resolve(url) onSuccess {
        case actualUrl: String => {
          HotList.recordShare(actualUrl, date)
          Last20Shares.addShare(Share(status.getUser.getScreenName, actualUrl))


          Actors.titleScraper ! LinkTitleScraperActor.GetTitleFor(actualUrl)
        }
      }
    }
  }

  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {
    //Logger.info("Status deleted: %s".format(statusDeletionNotice.getStatusId))
  }

  def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
    Logger.warn("Track limitation: %d".format(numberOfLimitedStatuses))
  }

  def onScrubGeo(userId: Long, upToStatusId: Long) {
    // wtf is this?
  }

  def onStallWarning(warning: StallWarning) {
    Logger.warn("Stall warning when listening for Guardian tweets: %s".format(warning.getMessage))
  }

  def onException(error: Exception) {
    Logger.warn("Encountered an exception listening for Guardian tweets: %s".format(error.getMessage))

    throw error
  }
}
