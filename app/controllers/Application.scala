package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory
import ranking.HotList
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import actors._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import models.ShareScore
import conf.Config

trait TitlesHelper {
  def getTitles(uris: Seq[String]): Future[Map[String, Option[String]]] = {
    implicit val timeout = Timeout(200 millis)

    val titlesFuture = Future.sequence(for {
      uri <- uris
    } yield (Actors.titleScraper ? LinkTitleScraperActor.GetTitleFor(uri)).mapTo[Option[String]] recover {
      case _ => None
    })

    titlesFuture map { titles => Map(uris zip titles: _*) }
  }
}

object Application extends Controller with TitlesHelper {
  val PageSize = 50

  def index = Action {
    val twitter = TwitterFactory.getSingleton

    if (twitter.getAuthorization.isEnabled) {
      Redirect(routes.Application.hotlist)
    } else {
      Redirect(routes.Twitter.index)
    }
  }

  def hotlist = Action {
    Async {
      for {
        links <- HotList.getHottest(20)
      } yield Ok(views.html.Hotlist.hotlist.render(Config.screenNameToFollow, links))
    }
  }

  def mostShares = Action {
    implicit val timeout = Timeout(300 millis)
    val oneDay = org.joda.time.Duration.standardDays(1)

    Async {
      for {
        shares <- ask(Actors.linkRanker, LinkRankingActor.GetTopShares(oneDay)).mapTo[List[(Int, String)]]
        page = shares take PageSize
        titles <- getTitles(page map { _._2 })
      } yield {

        val shares = for {
          (ranking, uri) <- page
        } yield ShareScore(uri, titles(uri) getOrElse uri, ranking)

        Ok(views.html.Hotlist.duration.render(Config.screenNameToFollow, shares))
      }
    }
  }

  def history(uri: String) = Action {
    implicit val timeout = Timeout(200 millis)

    Async {
      for {
        history <- ask(Actors.tweetsHistory, TweetsHistoryActor.GetHistoryFor(uri)).mapTo[TweetsHistory]
      } yield Ok(views.html.Hotlist.history.render(history.tweets))
    }
  }
}