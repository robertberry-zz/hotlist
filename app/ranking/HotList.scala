package ranking

import scala.collection.immutable.TreeSet
import akka.agent.Agent
import play.libs.Akka
import scala.math._

object HotList {
  implicit val actorSystem = Akka.system()

  private val rankings = Agent((TreeSet.empty[(Int, Link)], Map.empty[Link, Int]))

  private def calculateRank(link: Link): Int = {
    val seconds = (link.date.getMillis / 1000).toInt
    val logShares = log10(link.shares).toInt
    val gravity = 45000

    logShares + seconds / gravity
  }

  /** Adds the link if not present and updates its ranking */
  def rank(link: Link) {
    rankings send { pair =>
      val hotList = pair._1
      val ranking = pair._2

      // TODO need to include previous score somehow
      val newRank = calculateRank(link)

      val newHotList = (hotList - (ranking.getOrElse(link, 0) -> link)) + (newRank -> link)

      (newHotList, ranking + (link -> newRank))
    }
  }

  /** Returns a list of the current hottest links and their score */
  def getHottest: Stream[(Link, Int)] = {
    (rankings()._1.view map { case (score, link) => link -> score }).toStream
  }
}
