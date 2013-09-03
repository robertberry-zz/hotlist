package conf

import play.api.{Logger, Play}

object Config {
  lazy val screenNameToFollow: String = Play.current.configuration.getString("twitter.sn_to_follow").getOrElse({
    val errorMessage = "You must set twitter.sn_to_follow in the configuration file!"

    Logger.error(errorMessage)

    throw new RuntimeException(errorMessage)
  })
}
