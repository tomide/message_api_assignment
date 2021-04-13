package com.qlik.map.message.api

import cats.implicits._
import fs2.Stream
import monix.eval.TaskApp
import cats.effect.ExitCode
import com.qlik.map.message.api.config.ConfigManager
import com.qlik.map.message.api.database.MongoDbDatabase
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import nl.grons.metrics4.scala.DefaultInstrumented
import org.http4s.implicits._
import org.http4s.metrics.dropwizard.Dropwizard
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._
import scala.collection.immutable.Seq
import monix.execution.Scheduler.Implicits.global

/**
 * Main entry point to the Application. This object Binds the config parameter for bother server and database, routes, and connection to database together. it also the
 * binding point for middleware if we were to include them.
 * */


object MessageApiServer extends TaskApp with StrictLogging with DefaultInstrumented {


  def run(args: List[String]): Task[ExitCode] =
    for {
      configLocation <- Task.now(args.headOption) //implement command line parser
      _ <- Task(logger.info(
        s"""description="running with CLI args: ${}" """))
      exitCode <- serverStream(configLocation).compile.drain
        .as(ExitCode.Success)
        .onErrorHandleWith { t =>
          Task(logger.error("""description="Fatal failure" """, t)) *> Task.pure(ExitCode.Error)
        }
    } yield exitCode

  private def serverStream(configLocation: Option[String]): Stream[Task, ExitCode] =
    for {
      config <- Stream.apply(ConfigManager(configLocation))
      collection <- Stream.eval(MongoDbDatabase.getMongoDBCollection(config.DbParam))
      implicit0(messageService: MessageApiService) = new MessageApiServiceIO(collection)
      messageRoute = MessageApiRoutes.apply
      httpApp = Metrics(Dropwizard(metricRegistry, "server"))(
        Logger.httpRoutes(logHeaders = true, logBody = true)(messageRoute)
        ).orNotFound
      exitCode <- BlazeServerBuilder[Task]
        .withBanner(Seq("http4s Server starts ****************************"))
        .bindHttp(config.serverParam.port, config.serverParam.host)
        .withHttpApp(httpApp)
        .withNio2(true)
        .serve
    } yield exitCode



}
