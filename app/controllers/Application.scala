package controllers

import play.api._
import play.api.mvc._
import twitter4j.TwitterFactory

object Application extends Controller {
  
  def index = Action {
    if (TwitterFactory.getSingleton.getAuthorization.isEnabled) {
      Ok("You are authorized on Twitter")
    } else {
      Redirect(routes.Twitter.index)
    }
  }
  
}