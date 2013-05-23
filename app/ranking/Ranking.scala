package ranking

import org.joda.time.DateTime
import scala.math.log10
import twitter4j.Status

object Ranking {
  def rank(links: List[Link]): List[Link] = {
    links sortBy { l => score(l.shares, l.date) }
  }

  def score(shares: Double, date: DateTime): Double = {
    val seconds = date.getMillis / 1000
    val logShares = log10(shares)
    val gravity = 45000

    logShares + seconds / gravity
  }
}

case class Link(url: String, shares: Double, date: DateTime)