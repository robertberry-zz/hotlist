package actors

import akka.actor.Actor
import org.joda.time.DateTime
import lib.Time._
import com.github.nscala_time.time.Imports._
import scala.collection.immutable.TreeSet

object LinkRankingActor {
  type Shares = Map[String, TreeSet[DateTime]]

  sealed trait Message

  case class RecordShare(uri: String, time: DateTime) extends Message
  case object SpringCleaning extends Message
}

class LinkRankingActor extends Actor {
  import LinkRankingActor._

  var shares: Shares = Map.empty

  def receive = {
    case RecordShare(uri, time) =>
      shares = shares + (uri -> (shares.getOrElse(uri, TreeSet.empty[DateTime]) + time))

    case SpringCleaning => {
      val yesterday = DateTime.now() - 1.day

      shares = shares mapValues { _ from yesterday }
    }
  }
}
