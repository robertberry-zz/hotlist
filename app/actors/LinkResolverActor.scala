package actors

import dispatch._
import akka.actor.Actor
import akka.pattern.pipe
import com.twitter.util.LruMap
import scala.concurrent.ExecutionContext.Implicits.global

object LinkResolverActor {
  sealed trait Message

  case class Resolve(uri: String) extends Message
  case class CacheResolvedUri(uri: String, resolved: String) extends Message
}

class LinkResolverActor extends Actor {
  val cacheSize: Int = 50000

  import LinkResolverActor._

  val resolutionCache = new LruMap[String, String](cacheSize)

  def resolve(uri: String): Future[String] = for {
      response <- Http(url(uri).setFollowRedirects(true))
    } yield response.getUri.toString

  def receive = {
    case Resolve(uri) => resolutionCache.get(uri) match {
      case Some(resolvedUri) => sender ! resolvedUri
      case None => {
        val ftr = resolve(uri)

        ftr onSuccess {
          case resolved: String => self ! CacheResolvedUri(uri, resolved)
        }

        ftr pipeTo sender
      }
    }

    case CacheResolvedUri(uri, resolution) => resolutionCache += uri -> resolution
  }
}
