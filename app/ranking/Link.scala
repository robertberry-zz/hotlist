package ranking

import org.joda.time.DateTime

case class Link(url: String, title: Option[String], shares: Int, date: DateTime)