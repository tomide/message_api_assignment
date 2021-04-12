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
val cryptobitsVersion = "1.3"
val jwtCoreVersion = "7.1.1"
val pureConfig = "0.14.1"
val MongoDbJavaDriverVersion = "3.12.2"
val MongoDbScalaDriverVersion = "2.9.0"
val MongoDbScalaBsonVersion = "2.9.0"
val MongoDbDriveCoreVersion = "3.6.4"
val BsonVersion = "3.12.2"
val MongoDbDriveAsyncVersion = "3.6.4"
val nettyAllVersion = "4.1.63.Final"
val flapDoodleEmbedMongoDbTestVersion = "3.0.0"
val flapDoodleEmbedProcessTestVersion = "3.0.1"
val cashBashTestVersion = "3.1.1"
val scalaTestEmbedMongoDbTestVersion = "0.2.4"
val testContainersTestVersion = "1.15.2"
val MongoDbContainerTestVersion = "1.15.2"
val MongoDbJavaDriverTestVersion = "3.6.4"
val MongoDbDriverTestVersion =  "3.6.4"

lazy val root = (project in file("."))
  .settings(
    organization := "qlik",
    name := "message-API",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.2",
    scalacOptions +=  "-deprecation",
    libraryDependencies ++= Seq(
      "com.github.scopt"        %% "scopt" % Scopt,
      "com.typesafe"             % "config" % "1.4.1",
      "org.http4s"              %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"              %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"              %% "http4s-circe" % Http4sVersion,
      "org.http4s"              %% "http4s-dsl" % Http4sVersion,
      "org.http4s"              %% "http4s-dropwizard-metrics" % Http4sVersion,
      "io.circe"                %% "circe-generic" % CirceVersion,
      "io.circe"                %% "circe-generic-extras" % CirceVersion,
      "io.circe"                %% "circe-config" % CirceConfigVersion,
      "io.monix"                %% "monix" % MonixVersion,
      "org.tpolecat"            %% "doobie-core" % doobieVersion,
      "org.tpolecat"            %% "doobie-postgres" % doobieVersion,
      "org.tpolecat"            %% "doobie-hikari" % doobieVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % SLoggingVersion,
      "ch.qos.logback"           % "logback-classic" % LogbackVersion,
      "nl.grons"                %% "metrics4-scala" % Metrics4Scala,
      "org.typelevel"           %% "cats-core" % catsCore,
      "com.github.haifengl"      % "smile-core" % smile,
      "org.reactormonk"         %% "cryptobits"                    % cryptobitsVersion,
      "com.github.jwt-scala"    %% "jwt-core"                      % jwtCoreVersion,
      "com.github.pureconfig"   %% "pureconfig"                    % pureConfig,
      "org.mongodb"              % "mongo-java-driver"             % MongoDbJavaDriverVersion,
      "org.mongodb.scala"       %% "mongo-scala-driver"            % MongoDbScalaDriverVersion,
      "org.mongodb.scala"       %% "mongo-scala-bson"              % MongoDbScalaBsonVersion,
      "org.mongodb"              % "mongodb-driver-core"           % MongoDbDriveCoreVersion,
      "org.mongodb"              % "bson"                          % BsonVersion,
      "org.mongodb"              % "mongodb-driver-async"          % MongoDbDriveAsyncVersion,
      "io.netty"                 % "netty-all"                     % nettyAllVersion,
      "de.flapdoodle.embed"      %   "de.flapdoodle.embed.mongo"   % flapDoodleEmbedMongoDbTestVersion    % "test",
      "org.mongodb"             %%  "casbah"                       % cashBashTestVersion                  % "test",
      "com.github.simplyscala"  %%  "scalatest-embedmongo"         % scalaTestEmbedMongoDbTestVersion     % "test",
      "de.flapdoodle.embed"      % "de.flapdoodle.embed.process"   % flapDoodleEmbedProcessTestVersion    % "test",
      "org.testcontainers"       % "testcontainers"                % testContainersTestVersion            % Test,
      "org.testcontainers"       % "mongodb"                       % MongoDbContainerTestVersion          % Test,
      "org.mongodb"              % "mongo-java-driver"             % MongoDbJavaDriverTestVersion         % "test",
      "org.mongodb"              % "mongodb-driver"                % MongoDbDriverTestVersion             % "test",
      "org.scalatest"           %% "scalatest" % ScalaTestVersion % Test,
      "org.mockito"             %% "mockito-scala" % Mockito % Test,
      "com.opentable.components" % "otj-pg-embedded" % EmbeddedPG % Test,
      "com.squareup.okhttp3"     % "mockwebserver" % MockServer % Test
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


