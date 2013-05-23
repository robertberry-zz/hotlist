package ranking

import org.joda.time.DateTime
import scala.math.log10

object Ranking {
// TODO implement depending on eventual interface
//  def rank(tweets: List[Tweets]): List[Tweets] = {
//    tweets sortBy { t => score(t.shares, t.age) }
//  }

  def score(shares: Double, date: DateTime): Double = {
    val seconds = date.getMillis / 1000
    val logShares = log10(shares)
    val gravity = 45000

    logShares + seconds / gravity
  }
}
