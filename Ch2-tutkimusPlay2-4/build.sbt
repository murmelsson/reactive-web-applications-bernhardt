name := """twitter-stream"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

libraryDependencies += "com.ning" % "async-http-client" % "1.9.29"

resolvers += "Typesafe private" at "https://private-repo.typesafe.com/typesafe/maven-releases"

libraryDependencies += "com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0"