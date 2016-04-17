name := """test-reactive-aspects"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  "org.scalatest" %% "scalatest" % "2.2.1" % Test,
  "org.scalatestplus" %% "play" % "1.4.0-M4" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % Test
)

testOptions in Test += Tests.Argument(
  "-F",
  sys.props.getOrElse("SCALING_FACTOR", default = "1.0")
)

//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
