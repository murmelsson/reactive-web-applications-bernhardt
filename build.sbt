name := """scalangular-play"""

version := "1.0-SNAPSHOT"

//lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val scalaV = "2.11.7"

lazy val `ch08` = (project in file(".")).settings(
  scalaVersion := scalaV,
  scalajsProjects := Seq(client),
  pipelineStages := Seq(scalaJSProd),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.2"
  ),
  WebKeys.importDirectly := true
).enablePlugins(PlayScala).dependsOn(client).aggregate(client)

lazy val client = (project in file("modules/client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  ),
  skip in packageJSDependencies := false
).enablePlugins(ScalaJSPlugin, ScalaJSPlay, SbtWeb)



