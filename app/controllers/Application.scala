package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory
import ranking.HotList
import lib.TwitterAuthListener
import scala.concurrent.ExecutionContext.Implicits.global

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
}