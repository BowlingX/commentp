import com.mojolly.scalate.ScalatePlugin.ScalateKeys._
import com.mojolly.scalate.ScalatePlugin._
import org.scalatra.sbt._
import sbt.Keys._
import sbt._

object CommentpBuild extends Build {

  val projectOrg = "com.bowlingx"
  val projectName = "commentp"

  // library versions
  lazy val scalaVersionNum       = "2.11.6"
  lazy val scalatraVersion       = "2.4.0.RC1"
  lazy val reactiveMongoVersion  = "0.10.5.0.akka23"
  lazy val atmosphereVersion     = "2.3.0"
  lazy val jettyVersion          = "9.1.5.v20140505"
  lazy val servletApiVersion     = "3.1.0"
  lazy val logbackVersion        = "1.1.2"
  lazy val akkaVersion           = "2.3.9"
  lazy val guiceVersion          = "4.0"
  lazy val json4sVersion         = "3.2.11"
  lazy val scalamockVersion      = "3.2"

  lazy val project = Project(
    "commentp",
    file("."),
    settings = ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := projectOrg,
      name := projectName,
      mainClass in Compile := Some("com.bowlingx.commentp.launcher.Launcher"),
      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
      scalaVersion := scalaVersionNum,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Seq(
        // scalatra
        "org.scalatra" %% "scalatra" % scalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % scalatraVersion % "test",
        // logback
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        // jetty
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "compile;container",
        "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "compile;container",
        "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % "compile;container",
        "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion,
        "javax.servlet" % "javax.servlet-api" % servletApiVersion,
        "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
        // atmosphere
        "org.atmosphere" % "atmosphere-runtime" % atmosphereVersion,
        "org.atmosphere" % "atmosphere-guice" % atmosphereVersion,
        // guice
        "com.google.inject" % "guice" % guiceVersion,
        "com.tzavellas" %% "sse-guice" % "0.7.2",

        // Akka
        "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-remote" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
        "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "org.json4s" %% "json4s-native" % json4sVersion,
        "org.json4s" %% "json4s-jackson" % json4sVersion,

        // mocking
        "org.scalamock" %% "scalamock-scalatest-support" % scalamockVersion % "test"

      ),
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile) { base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty, /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ), /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
