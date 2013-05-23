package models

import org.squeryl.Schema

object AppDB extends Schema {
  val twitterAccessTokenTable = table[TwitterAccessToken]("twitter_access_token")
}
