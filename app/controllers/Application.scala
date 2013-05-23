package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory

object Application extends Controller {
  
  def index = Action {
    val twitter = TwitterFactory.getSingleton

    if (twitter.getAuthorization.isEnabled) {
      val username = twitter.getScreenName

      Ok("You are authorized on Twitter as " + username)
    } else {
      Redirect(routes.Twitter.index)
    }
  }
  
}