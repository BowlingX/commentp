import NativePackagerHelper._

enablePlugins(JavaServerAppPackaging)

maintainer in Linux := "David Heidrich <me@bowlingx.com>"

packageSummary in Linux := "commentp - REST-API for commentp"

packageDescription := "This is the REST-API Server for commentp"

daemonUser in Linux := normalizedName.value

daemonGroup in Linux := (daemonUser in Linux).value

mappings in Universal ++= directory("src/main/webapp")

// checkstyle when compile

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle
