package lib

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import dispatch._
import org.jsoup.Jsoup
import play.api.Logger
import scala.util.{Failure, Try}

case class LinkSummary(title: String, imageLink: String)

object LinkScraper {

  private val links = new ConcurrentHashMap[String, LinkSummary]().asScala

  def getSummary(link: String): Option[LinkSummary] = links.get(link)

  def scrape(link: String) {
    val svc = url(link)

    val response = Http(svc OK as.String)

    response onSuccess {
      case contentBody => {
        val document = Jsoup.parse(contentBody)

        val title = Try {
          document.select("title").first().text()
        }

        title match {
          case Failure(e: Exception) => Logger.info("Could not extract title for %s: %s".format(link, e.getMessage))
          case _ =>
        }

        val firstImage = Try {
          document.select("img").first().attr("src")
        }

        firstImage match {
          case Failure(e: Exception) => Logger.info("Could not extract image for %s: %s".format(link, e.getMessage))
          case _ =>
        }

        links.put(link, LinkSummary(title.getOrElse("No title"), firstImage.getOrElse("")))
      }
    }
  }
}
