package actors

import akka.actor.Actor
import org.joda.time.DateTime
import lib.Time._
import com.github.nscala_time.time.Imports._
import scala.collection.immutable.TreeSet
import scala.concurrent.Future
import akka.pattern.pipe
import scala.concurrent.ExecutionContext.Implicits.global

object LinkRankingActor {
  type Shares = Map[String, TreeSet[DateTime]]

  sealed trait Message

  case class RecordShare(uri: String, time: DateTime) extends Message
  case object SpringCleaning extends Message
  case class GetTopShares(duration: Duration) extends Message
}

class LinkRankingActor extends Actor {
  import LinkRankingActor._

  var shares: Shares = Map.empty

  def receive = {
    case RecordShare(uri, time) => {
      shares = shares + (uri -> (shares.getOrElse(uri, TreeSet.empty[DateTime]) + time))
    }

    case SpringCleaning => {
      val yesterday = DateTime.now() - 1.day

      shares = shares mapValues { _ from yesterday }
    }

    case GetTopShares(duration) => {
      Future {
        val limit = DateTime.now() - duration

        val rankingsForPeriod = for {
          (uri, shares) <- shares.toList
        } yield {
          // not going to prematurely optimize yet, but .size here is actually log(n) due to the underlying RB tree
          // implementation
          shares.from(limit).size -> uri
        }

        rankingsForPeriod.sortBy(-_._1)
      } pipeTo sender
    }
  }
}
