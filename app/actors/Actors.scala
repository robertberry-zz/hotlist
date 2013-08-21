package actors

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Actors {
  val system = ActorSystem.create("hotlist")

  val linkResolver = system.actorOf(Props[LinkResolverActor], name="link_resolver")
  val titleScraper = system.actorOf(Props[LinkTitleScraperActor], name="title_scraper")
  val tweetsHistory = system.actorOf(Props[TweetsHistoryActor], name="tweets_history")
  val linkRanker = system.actorOf(Props[LinkRankingActor], name="link_ranker")

  /** Regularly clean up links older than 24 hours */
  system.scheduler.schedule(5 minutes, 5 minutes) {
    linkRanker ! LinkRankingActor.SpringCleaning
  }
}
