package actors

import dispatch._
import org.jsoup.Jsoup
import scala.util.Try
import akka.actor.Actor
import com.twitter.util.LruMap
import scala.concurrent.ExecutionContext.Implicits.global

object LinkTitleScraperActor {
  sealed trait Message

  case class GetTitleFor(uri: String)
  case class GetTitlesFor(uris: List[String])
  case class SetTitleFor(uri: String, title: String)
}

class LinkTitleScraperActor extends Actor {
  import LinkTitleScraperActor._

  val cacheSize: Int = 50000

  val cache = new LruMap[String, String](cacheSize)

  def scrapeTitle(uri: String): Future[String] = for {
      response <- Http(url(uri) OK as.String)
    } yield Try { Jsoup.parse(response).select("title").first().text() }.getOrElse("Untitled")

  def receive = {
    case GetTitleFor(uri) => cache.get(uri) match {
      case Some(title) => sender ! Some(title)
      case None => sender ! None; scrapeTitle(uri) onSuccess {
        case title => self ! SetTitleFor(uri, title)
      }
    }

    case SetTitleFor(uri, title) => cache += uri -> title
  }
}
