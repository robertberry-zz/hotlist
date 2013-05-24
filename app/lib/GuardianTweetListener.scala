package lib

import twitter4j.{Status, StatusDeletionNotice, StallWarning, StatusListener}
import play.api.Logger
import org.joda.time.DateTime
import ranking.{HotList, StatusUtils}

class GuardianTweetListener extends StatusListener {
  def onStatus(status: Status) {
    Logger.info("Got status: %s says %s".format(status.getUser.getScreenName, status.getText))

    val urls = StatusUtils.extractUrls(status.getText)
    val date = new DateTime(status.getCreatedAt)
    urls foreach { url => HotList.recordShare(url, date) }
  }

  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {
    Logger.info("Status deleted: %s".format(statusDeletionNotice.getStatusId))
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
  }
}
