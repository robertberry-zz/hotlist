package lib

import twitter4j.TwitterException

object TwitterRateLimitError {
  def unapply(error: TwitterException) = {
    if (error.exceededRateLimitation()) {
      Some(error)
    } else {
      None
    }
  }
}
