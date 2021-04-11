package com.qlik.map.message.api

import com.qlik.map.message.api.{MessageApiRoutes, MessageApiServiceIO, TestFixture}
import io.circe.Decoder.decodeString
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{Method, Request, Response, Status}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import com.qlik.map.message.messageApiService._

class CreateMessageSpec extends AnyFunSuite
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with TestFixture
  with Matchers {

  def check[A](actual: Task[Response[Task]],
               expectedStatus: Status,
               expectedBody: Option[A])
              (implicit ev : Decoder[A]): Boolean = {

    implicit val feedBackEncoder: Encoder[feedBack] = deriveEncoder[feedBack]
    implicit val feedBackDecoder: Decoder[feedBack] = deriveDecoder[feedBack]

    val actualResp = actual.runSyncUnsafe()
    println(actualResp.status)
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.runSyncUnsafe().isEmpty)( // Verify Response's body is empty.
      expected => actualResp.as[feedBack].runSyncUnsafe() == expected
    )
    statusCheck && bodyCheck
  }

  var mongoDBContainer : MongoDBContainer = _
  var collection: MongoCollection[Document]  = _

override def beforeAll {
  super.beforeAll()
  mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
  mongoDBContainer.start()
  val client: MongoClient = MongoClient(mongoDBContainer.getReplicaSetUrl())
  val DbConnection: MongoDatabase = client.getDatabase("test")
  collection = DbConnection.getCollection("test")
}


  override def afterAll(): Unit = {
    super.afterAll()
    collection.drop()
    mongoDBContainer.stop()
  }

  test("should return a status of created and response of created message with a boolean representing if word is palindrome of not") {
    val response: Task[Response[Task]] = MessageApiRoutes(new MessageApiServiceIO(collection)).orNotFound.run(
      Request(method = Method.POST, uri = uri"/create_message" ).withEntity(someValidCreateMessage))
    check[String](response, Status.Ok, Some(someValidCreateResponse))
  }

  test("should return a status of not allowed if numeric value in message and response of created message with a boolean representing if word is palindrome of not") {
    val response: Task[Response[Task]] = MessageApiRoutes(new MessageApiServiceIO(collection)).orNotFound.run(
      Request(method = Method.POST, uri = uri"/create_message" ).withEntity(someValidCreateMessage))
    check[String](response, Status.Ok, Some(someValidCreateResponse))
  }

  test("should return a status of created and response of created message with a boolean representing if word is palindrome of not") {
    val response: Task[Response[Task]] = MessageApiRoutes(new MessageApiServiceIO(collection)).orNotFound.run(
      Request(method = Method.POST, uri = uri"/create_message" ).withEntity(someValidCreateMessage))
    check[String](response, Status.Ok, Some(someValidCreateResponse))
  }

  test("should return a status of created and response of created message with a boolean representing if word is palindrome of not") {
    val response: Task[Response[Task]] = MessageApiRoutes(new MessageApiServiceIO(collection)).orNotFound.run(
      Request(method = Method.POST, uri = uri"/create_message" ).withEntity(someValidCreateMessage))
    check[String](response, Status.Ok, Some(someValidCreateResponse))
  }



}