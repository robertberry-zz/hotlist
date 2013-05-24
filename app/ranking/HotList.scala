package ranking

import scala.collection.immutable.TreeSet
import akka.agent.Agent
import play.libs.Akka
import scala.math._
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import java.util.concurrent.atomic.AtomicInteger
import org.joda.time.DateTime
import play.api.Logger
import lib.LinkScraper

object HotList {
  implicit val actorSystem = Akka.system()

  private val rankings = Agent((TreeSet.empty[(Int, String)], Map.empty[String, Int]))

  private val shares = new ConcurrentHashMap[String, AtomicInteger]().asScala

  private val firstSeen = new ConcurrentHashMap[String, DateTime]().asScala

  private def calculateRank(link: String): Int = {
    val age = firstSeen.getOrElse(link, {
      throw new RuntimeException("Age not recorded for link - should never occur")
    })
    val nShares = shares.getOrElse(link, {
      throw new RuntimeException("Shares not recorded for link - should never occur")
    }).get

    val seconds = (age.getMillis / 1000).toInt
    val logShares = log10(nShares).toInt
    val gravity = 45000

    val rank = logShares + seconds / gravity

    Logger.info("New rank for %s: %d".format(link, rank))

    rank
  }

  /** Record a share for the given link, initially seen at the given time */
  def recordShare(link: String, date: DateTime) {
    firstSeen.putIfAbsent(link, date)

    shares.putIfAbsent(link, new AtomicInteger(1)) match {
      case Some(numberOfShares) => numberOfShares.incrementAndGet()
      case _ => // pass
    }

    rank(link)
  }

  /** Updates the ranking for a link (eventually) */
  private def rank(link: String) {
    rankings send { pair =>
      val hotList = pair._1
      val ranking = pair._2

      val newRank = calculateRank(link)

      val newHotList = (hotList - (ranking.getOrElse(link, 0) -> link)) + (newRank -> link)

      (newHotList, ranking + (link -> newRank))
    }
  }

  /** Returns a list of the current hottest links and their score */
  def getHottest: Stream[Link] = {
    (rankings()._1.view map { case (score, link) =>
      val summary = LinkScraper.getSummary(link)

      Link(link,
        summary map { _.title },
        summary map { _.imageLink },
        shares.get(link).map(_.get).getOrElse(1),
        firstSeen.get(link).getOrElse(new DateTime()))
    }).toStream
  }
}
