package lib

import dispatch._
import scala.collection.JavaConverters._
import java.util.concurrent.ConcurrentHashMap
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

object LinkResolver {
  private val linkCache = new ConcurrentHashMap[String, String].asScala

  def resolveLink(link: String): Future[String] = {
    linkCache.get(link) map { Future.successful(_) } getOrElse {
      val svc = url(link)

      val actualLinkFuture = for {
        response <- Http(svc)
      } yield response.getUri.toString

      actualLinkFuture onSuccess {
        case actualLink: String => {
          Logger.info("Resolved link: %s -> %s".format(link, actualLink))

          linkCache.putIfAbsent(link, actualLink)
        }
      }

      actualLinkFuture
    }
  }
}
