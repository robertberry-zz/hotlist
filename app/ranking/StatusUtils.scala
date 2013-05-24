package ranking

import scala.util.Try
import java.net.URL

object StatusUtils {
  def extractUrls(body: String): List[String] = {
    val url = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".r

    val matches = url findAllIn body filter {
      url => Try { new URL(url) }.isSuccess
    }

    // don't include Guardian urls as we're tracking other content that is popular
    matches filterNot { isGuardianUrl_? _ }

    matches.toList
  }

  def isGuardianUrl_?(url: String): Boolean = {
    val host = new URL(url).getHost

    host.contains("guardian.co.uk") || host.contains("theguardian.com")
  }
}