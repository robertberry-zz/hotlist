package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory
import ranking.HotList
import lib.TwitterAuthListener

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
    val links = HotList.getHottest take(20)
    // TODO put screen name in better place
    Ok(views.html.Hotlist.hotlist.render(TwitterAuthListener.screenNameToFollow, links))
  }
}