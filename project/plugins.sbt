addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.5.0")

addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "2.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"