enablePlugins(JavaServerAppPackaging)

maintainer in Linux := "David Heidrich <me@bowlingx.com>"

packageSummary in Linux := "commentp - REST-API for commentp"

packageDescription := "This is the REST-API Server for commentp"

daemonUser in Linux := normalizedName.value

daemonGroup in Linux := (daemonUser in Linux).value
