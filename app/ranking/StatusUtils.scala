package ranking

object StatusUtils {
  def extractUrls(body: String): List[String] = {
    val url = """(https?|ftp)://(.*)\.([a-z]+)""".r
    val matches = url findAllIn body
    matches.toList
  }
}