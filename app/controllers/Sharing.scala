package controllers

import play.api._
import play.api.mvc.{Action, Controller}

object Sharing extends Controller {
  def index = Action {
     Ok("hi")
  }
}
