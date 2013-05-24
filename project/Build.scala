import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "hotlist"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
    "org.jsoup" % "jsoup" % "1.6.1",
    "com.typesafe.akka" % "akka-agent_2.10" % "2.1.4",
    "org.twitter4j" % "twitter4j-core" % "3.0.3",
    "org.twitter4j" % "twitter4j-async" % "3.0.3",
    "org.twitter4j" % "twitter4j-stream" % "3.0.3",
    "org.squeryl" %% "squeryl" % "0.9.5-6",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
