package lib

import akka.agent.Agent
import play.libs.Akka

object Last20Shares {
  implicit val actorSystem = Akka.system()

  private val shares = Agent(List[Share]())

  def addShare(share: Share) {
    shares send { xs: List[Share] =>
      (share :: xs) take 20
    }
  }

  def last20 = shares.get
}
