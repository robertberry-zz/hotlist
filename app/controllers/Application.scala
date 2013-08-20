package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory
import ranking.HotList
import lib.TwitterAuthListener
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import actors.{TweetsHistory, TweetsHistoryActor, Actors}
import akka.util.Timeout
import scala.concurrent.duration._

object Application extends Controller {
  
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
      } yield Ok(views.html.Hotlist.hotlist.render(TwitterAuthListener.screenNameToFollow, links))
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