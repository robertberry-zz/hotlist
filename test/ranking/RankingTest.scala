package ranking

import org.specs2.mutable._

class RankingTest extends Specification {
  "Extract URLs" should {
    "extract urls from a string of text" in {
      val body1 = "My string with a url in it http://guardian.com"
      val body2 = "My string with a url in it http://guardian.com and some other text"
      val res1 = StatusUtils.extractUrls(body1)
      val res2 = StatusUtils.extractUrls(body2)
      val expectedLinks = List("http://guardian.com")

      res1 mustEqual expectedLinks
      res2 mustEqual expectedLinks
    }
  }
}
