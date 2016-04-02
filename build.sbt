name := """dealing-with-state"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  "org.postgresql" % "postgresql" % "9.3-1101-jdbc4",
  "org.jooq" % "jooq" % "3.7.0",
  "org.jooq" % "jooq-codegen-maven" % "3.7.0",
  "org.jooq" % "jooq-meta" % "3.7.0"
)

//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (sourceManaged, fullClasspath in Compile, runner in Compile, streams) map { (src, cp, r, s) =>
  toError(r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/chapter7.xml"), s.log))
  ((src / "main/generated") ** "*.scala").get
}

generateJOOQ <<= generateJOOQTask

unmanagedSourceDirectories in Compile +=
  sourceManaged.value / "main/generated"

routesGenerator := InjectedRoutesGenerator



