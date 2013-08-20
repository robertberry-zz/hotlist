package models

import twitter4j.Status
import com.github.nscala_time.time.Imports._

object Tweet {
  def apply(status: Status): Tweet = {
    new Tweet(status.getUser.getScreenName, status.getText, new DateTime(status.getCreatedAt))
  }
}

case class Tweet(username: String, message: String, tweetedAt: DateTime)
