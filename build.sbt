val Http4sVersion = "0.21.1"
val CirceVersion = "0.13.0"
val LogbackVersion = "1.2.3"
val MonixVersion = "3.1.0"
val doobieVersion = "0.8.8"
val ScalaTestVersion = "3.1.1"
val SLoggingVersion = "3.9.2"
val CirceConfigVersion = "0.8.0"
val Metrics4Scala = "4.1.5"
val Scopt = "4.0.0-RC2"
val Mockito = "1.14.3"
val EmbeddedPG = "0.13.3"
val MockServer = "4.3.1"
val catsCore = "2.1.1"
val smile = "2.6.0"

lazy val root = (project in file("."))
  .settings(
    organization := "qlik",
    name := "message-API",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq( "-deprecation"),
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % Scopt,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-dropwizard-metrics" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceVersion,
      "io.circe" %% "circe-config" % CirceConfigVersion,
      "io.monix" %% "monix" % MonixVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % SLoggingVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "nl.grons" %% "metrics4-scala" % Metrics4Scala,
      "org.typelevel" %% "cats-core" % catsCore,
      "com.github.haifengl" % "smile-core" % smile,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.mockito" %% "mockito-scala" % Mockito % Test,
      "com.opentable.components" % "otj-pg-embedded" % EmbeddedPG % Test,
      "com.squareup.okhttp3" % "mockwebserver" % MockServer % Test,
      "org.reactormonk" %% "cryptobits" % "1.3",
      "com.github.jwt-scala" %% "jwt-core" % "7.1.1",
      "com.github.pureconfig" %% "pureconfig" % "0.14.1",
      "org.mongodb" % "mongo-java-driver" % "3.12.2",
      "org.mongodb" % "mongo-java-driver" % "3.6.4"    % "test",
      "org.mongodb" % "mongodb-driver" % "3.6.4"    % "test",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
      "org.mongodb.scala" %% "mongo-scala-bson" % "2.9.0",
      "org.mongodb" % "mongodb-driver-core" % "3.6.4",
      "org.mongodb" % "bson" % "3.12.2",
      "org.mongodb" % "mongodb-driver-async" % "3.6.4",
      "io.netty" % "netty-buffer" % "4.1.63.Final",
      "io.netty" % "netty-common" % "4.1.63.Final",
      "io.netty" % "netty-all" % "4.1.63.Final",
      "de.flapdoodle.embed"         %   "de.flapdoodle.embed.mongo"   % "1.48.0"    % "test",
      "org.mongodb"                 %%  "casbah"                      % "3.1.1"     % "test",
      "com.github.simplyscala"      %%  "scalatest-embedmongo"        % "0.2.4"     % "test",
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
assemblyJarName in assembly := "messageApiService.jar"

cancelable in Global := true
fork in Global := true


