import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

// docker:

packageName in Docker := "bowlingx/commentp"

maintainer in Docker := "David Heidrich <me@bowlingx.com>"

dockerBaseImage := "bowlingx/commentp-base"

dockerExposedPorts := Seq(8080)

daemonUser in Docker := "commentp"

javaOptions in Docker ++= Seq(
  // JVM memory tuning
  "-J-Xmx1.5g",
  "-J-Xms1g"
)