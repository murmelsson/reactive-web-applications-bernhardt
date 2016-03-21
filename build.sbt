name := """twitter-stream"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Typesafe private" at
  "https://private-repo.typesafe.com/typesafe/maven-releases"
libraryDependencies +=
  "com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0"
