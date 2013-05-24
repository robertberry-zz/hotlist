package ranking

import org.joda.time.DateTime

case class Link(url: String, title: Option[String], image: Option[String], shares: Int, date: DateTime)