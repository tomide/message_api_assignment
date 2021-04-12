package com.qlik.map.message.api

import io.circe.Decoder.decodeString
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{Method, Request, Response, Status}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ListMessageSpec extends AnyFunSuite
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with TestFixture
  with Matchers {
  import com.qlik.map.message.messageApiService._

  def check[A](actual: Task[Response[Task]],
               expectedStatus: Status,
               expectedBody: Option[A])
              (implicit ev : Decoder[A]): Boolean = {

    implicit val feedBackEncoder: Encoder[feedBack] = deriveEncoder[feedBack]
    implicit val feedBackDecoder: Decoder[feedBack] = deriveDecoder[feedBack]

    val actualResp = actual.runSyncUnsafe()
    println(actualResp.status)
    val statusCheck = actualResp.status == expectedStatus
    statusCheck
  }

  var mongoDBContainer : MongoDBContainer = _
  var collection: MongoCollection[Document]  = _

  override def beforeAll {
    super.beforeAll()
    mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
    mongoDBContainer.start()
    val client: MongoClient = MongoClient(mongoDBContainer.getReplicaSetUrl())
    val DbConnection: MongoDatabase = client.getDatabase("myFirstDatabase")
    collection = DbConnection.getCollection("test")
    Await.ready(collection.insertMany(someValidDocument).toFuture(), Duration.Inf).value.get
  }

  override def afterAll(): Unit = {
    super.afterAll()
    collection.drop()
    mongoDBContainer.stop()
  }

  test("should return all messages in the database") {
    val response: Task[Response[Task]] = MessageApiRoutes(new MessageApiServiceIO(collection)).orNotFound.run(
      Request(method = Method.GET, uri = uri"/list_messages" ).withEntity(someValidCreateMessage))
    assert(check[String](response, Status.Ok, Some(someValidCreateResponse))).shouldBe(true)
  }

}





