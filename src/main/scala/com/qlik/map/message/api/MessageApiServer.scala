package com.qlik.map.message.api

import cats.effect.ExitCode
import cats.implicits._
import fs2.Stream
import monix.eval.{Task, TaskApp}
import cats.effect.ExitCode
import com.qlik.map.message.api.database.Database
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import nl.grons.metrics4.scala.DefaultInstrumented
import org.http4s.implicits._
import org.http4s.metrics.dropwizard.Dropwizard
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware._

import scala.collection.immutable.Seq


object MessageApiServer extends TaskApp with StrictLogging with DefaultInstrumented {


  def run(args: List[String]): Task[ExitCode] =
    for {
      exitCode <- serverStream().compile.drain
        .as(ExitCode.Success)
        .onErrorHandleWith { t =>
          Task(logger.error("""description="Fatal failure" """, t)) *> Task.pure(ExitCode.Error)
        }
    } yield exitCode

  private def serverStream(): Stream[Task, ExitCode] =
    for {
      collection <- Stream.eval(Database.getMongoDatabase)
      implicit0(messageService: MessageApiService) = new MessageApiServiceIO(collection)
      messageRoute = MessageApiRoutes.apply
      httpApp = Metrics(Dropwizard(metricRegistry, "server"))(
        Logger.httpRoutes(logHeaders = true, logBody = true)(messageRoute)
        ).orNotFound
      exitCode <- BlazeServerBuilder[Task]
        .withBanner(Seq("http4s Server starts ****************************".toString))
        //        .bindHttp(sys.env("PORT").toInt, "0.0.0.0")
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .withNio2(true)
        .serve
    } yield exitCode



}
