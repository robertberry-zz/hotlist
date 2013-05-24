package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory
import ranking.HotList

object Application extends Controller {
  
  def index = Action {
    val twitter = TwitterFactory.getSingleton

    if (twitter.getAuthorization.isEnabled) {
      val username = twitter.getScreenName
      Redirect(routes.Application.hotlist)
    } else {
      Redirect(routes.Twitter.index)
    }
  }

  def hotlist = Action {
    val links = HotList.getHottest take(20) sortBy { _._2 } map { _._1 }
    Ok(views.html.Hotlist.hotlist.render(links))
  }
}