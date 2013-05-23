package ranking

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime

class RankingTest extends Specification {
  "The ranking algorithm" should {
    "score items based on date and age" in {
      val date = new DateTime()

      val s1 = Ranking.score(10, date)
      val s2 = Ranking.score(5, date)

      s1 must be greaterThanOrEqualTo(s2)
    }
  }
}
