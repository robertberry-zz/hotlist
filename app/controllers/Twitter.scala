package controllers

import play.api._
import play.api.mvc._
import scala.util.{Failure, Success, Try}
import twitter4j.{TwitterException, TwitterFactory}
import java.util.concurrent.atomic.AtomicReference
import twitter4j.auth.RequestToken
import play.api.data._
import play.api.data.Forms._
import models.{TwitterAccessToken, AppDB}
import lib.TwitterAuthListener
import org.squeryl.PrimitiveTypeMode.inTransaction

object Twitter extends Controller {
  type TokenWithAuthUrl = (RequestToken, String)

  val lastRequestToken = new AtomicReference[Option[TokenWithAuthUrl]](None)

  val authenticationForm = Form(
    ("pin" -> text)
  )

  def index = Action {
    val twitter = TwitterFactory.getSingleton

    val requestToken = Try { twitter.getOAuthRequestToken }

    requestToken match {
      case Success(token) => {
        val authUrl = token.getAuthenticationURL

        lastRequestToken.set(Some(token, authUrl))

        Ok(views.html.Twitter.index.render(authenticationForm, authUrl))
      }

      case Failure(error: TwitterException) if error.getStatusCode == 401 => {
        Ok("Bad key / secret combination: %s".format(error.getMessage))
      }

      case Failure(error) => throw error
    }
  }

  def authenticate = Action { implicit request =>
    lastRequestToken.get match {
      case Some((requestToken, authUrl)) => {
        authenticationForm.bindFromRequest.fold(
          formWithErrors => BadRequest("error"),

          pin => {
            val twitter = TwitterFactory.getSingleton

            val accessToken = Try {
              if (pin.nonEmpty) {
                twitter.getOAuthAccessToken(requestToken, pin)
              } else {
                twitter.getOAuthAccessToken
              }
            }

            accessToken match {
              case Success(t) => {
                // store to persist token here
                inTransaction {
                  AppDB.twitterAccessTokenTable insert TwitterAccessToken(t)
                }

                TwitterAuthListener.onAuthenticate(t)

                Logger.info("User %s (%d) authenticated against Twitter: %s, %s".format(
                  t.getScreenName, t.getUserId, t.getToken, t.getTokenSecret))

                Redirect(routes.Application.hotlist)
              }
              case Failure(error) => {
                Ok("Could not authenticate: %s".format(error.getMessage))
              }
            }
          }
        )
      }

      case None => BadRequest("No request token set")
    }
  }
}
