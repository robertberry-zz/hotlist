package lib

import org.joda.time.DateTime

object Time {
  implicit val DateTimeOrdering = new Ordering[DateTime] {
    def compare(a: DateTime, b: DateTime) = a.compareTo(b)
  }
}
