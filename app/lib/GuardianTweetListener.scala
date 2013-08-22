package lib

import twitter4j.{Status, StatusDeletionNotice, StallWarning, StatusListener}
import play.api.Logger
import org.joda.time.DateTime
import ranking.{HotList, StatusUtils}
import scala.concurrent.ExecutionContext.Implicits.global
import actors._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import models.Tweet

object Retweet {
  def unapply(status: Status) = {
    if (status.isRetweet) {
      Some(status.getRetweetedStatus)
    } else {
      None
    }
  }
}

class GuardianTweetListener extends StatusListener {
  implicit val urlResolutionTimeout = Timeout(1 second)

  def onStatus(status: Status) {
    status match {
      case Retweet(original) => {
        Logger.info(s"${status.getUser.getScreenName} retweeted ${original.getUser.getScreenName}: ${original.getText}")
      }
      case _ => {
        Logger.info(s"${status.getUser.getScreenName} tweeted ${status.getText}")
      }
    }

    val sourceText = sourceTextFor(status)

    val urls = StatusUtils.extractUrls(sourceText)
    val date = new DateTime(status.getCreatedAt)
    urls foreach { url =>
      Actors.linkResolver ? LinkResolverActor.Resolve(url) onSuccess {
        case actualUrl: String => {
          HotList.recordShare(actualUrl, date)
          Last20Shares.addShare(Share(status.getUser.getScreenName, actualUrl))

          Actors.titleScraper ! LinkTitleScraperActor.GetTitleFor(actualUrl)
          Actors.tweetsHistory ! TweetsHistoryActor.AddToHistory(actualUrl, Tweet(status))
          Actors.linkRanker ! LinkRankingActor.RecordShare(actualUrl, new DateTime(status.getCreatedAt))
        }
      }
    }
  }

  def sourceTextFor(status: Status) = {
    status match {
      case Retweet(original) => original.getText
      case _ => status.getText
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
