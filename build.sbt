// Need this import to avoid type error in expression: not found : value ServerLoader
// when loading project to sbt:
import com.typesafe.sbt.packager.archetypes.ServerLoader

name := """simple-app-deploy-it"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(
  PlayScala,
  DebianPlugin,
  JavaServerAppPackaging
)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.1.4",
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.53.0",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M4" % "test"
)

//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs)

RjsKeys.mainModule := "application"

RjsKeys.mainConfig := "application"

maintainer := "dylan drummond <dylan@murmelssonic.fi>"

packageSummary in Linux := "simple-app-deploy-it, a PlayFramework app"

packageDescription := "This package installs the simple-app-deploy-it (following Ch10 of Manuel Bernhardt's most excellent book (Reactive Web Applications))"

serverLoading in Debian := ServerLoader.Systemd

dockerExposedPorts in Docker := Seq(9000, 9443)

