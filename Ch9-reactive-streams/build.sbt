name := """reactive-streams"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  "com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0",
  "com.typesafe.play" %% "play-streams-experimental" % "2.4.2",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0",
  "com.ning" % "async-http-client" % "1.9.29"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
