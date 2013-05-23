package models

import org.squeryl.{Schema, KeyedEntity}
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

