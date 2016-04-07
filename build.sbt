name := """scalangular-play"""

version := "1.0-SNAPSHOT"

//lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val scalaV = "2.11.7"

lazy val `ch08` = (project in file(".")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages := Seq(scalaJSProd),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.2",
    jdbc,
    "org.postgresql" % "postgresql" % "9.3-1101-jdbc4",
    "org.jooq" % "jooq" % "3.7.0",
    "org.jooq" % "jooq-codegen-maven" % "3.7.0",
    "org.jooq" % "jooq-meta" % "3.7.0"
  ),
  WebKeys.importDirectly := true
).enablePlugins(PlayScala).dependsOn(client).aggregate(client)

lazy val client = (project in file("modules/client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  scalaJSStage in Global := FastOptStage,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "biz.enef" %%% "scalajs-angulate" % "0.2"
  ),
  jsDependencies ++= Seq(
    "org.webjars.bower" % "angular" % "1.4.0" / "angular.min.js",
    "org.webjars.bower" % "angular-route" % "1.4.0" /
      "angular-route.min.js" dependsOn "angular.min.js",
    "org.webjars.bower" % "angular-websocket" % "1.0.13" /
      "dist/angular-websocket.min.js" dependsOn "angular.min.js"
  ),
  skip in packageJSDependencies := false
).enablePlugins(ScalaJSPlugin, ScalaJSPlay, SbtWeb)

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (sourceManaged, fullClasspath in Compile, runner in Compile, streams) map { (src, cp, r, s) =>
  toError(r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/chapter7.xml"), s.log))
  ((src / "main/generated") ** "*.scala").get
}

generateJOOQ <<= generateJOOQTask

unmanagedSourceDirectories in Compile += sourceManaged.value / "main/generated"


