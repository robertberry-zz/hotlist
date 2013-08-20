package actors

import akka.actor.{Props, ActorSystem}

object Actors {

  val system = ActorSystem.create("hotlist")

  val linkResolver = system.actorOf(Props[LinkResolverActor], name="link_resolver")
  val titleScraper = system.actorOf(Props[LinkTitleScraperActor], name="title_scraper")
  val tweetsHistory = system.actorOf(Props[TweetsHistoryActor], name="tweets_history")
}
