package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import twitter4j.auth.AccessToken

class TwitterAccessToken(
  token: String,
  secret: String
) extends KeyedEntity[Long] {
  val id: Long = 0

  def toToken: AccessToken = {
    new AccessToken(token, secret)
  }
}

object TwitterAccessToken {
  def apply(accessToken: AccessToken): TwitterAccessToken =
    new TwitterAccessToken(accessToken.getToken, accessToken.getTokenSecret)

  def last: Option[TwitterAccessToken] = (from(AppDB.twitterAccessTokenTable) { (accessToken) =>
      select(accessToken) orderBy(accessToken.id desc)
    }).headOption
}
