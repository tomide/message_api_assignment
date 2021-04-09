//package com.qlik.map.message.api
//
//import java.util.concurrent.Executors
//
//import cats.effect.{Blocker, ExitCode}
//import com.codahale.metrics.MetricFilter
//import com.qlik.map.messages.api.MessageApiServer
//import com.qlik.map.messages.api.Util.md5Harsher
//import monix.eval.Task
//import monix.execution.CancelableFuture
//import monix.execution.Scheduler.Implicits.global
//import okhttp3.mockwebserver.{MockResponse, MockWebServer}
//import org.http4s.client.{Client, JavaNetClientBuilder}
//import org.http4s.implicits.http4sLiteralsSyntax
//import org.http4s.{Method, Request, Status}
//import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
//import org.scalatest.OptionValues.convertOptionToValuable
//import org.scalatest.featurespec.AnyFeatureSpec
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen, OneInstancePerTest}
//import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
//import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
//import org.scalatest.funspec.AnyFunSpec
//
//import scala.jdk.CollectionConverters.mapAsScalaMapConverter
//import java.util.Date
//
//import com.github.simplyscala.MongoEmbedDatabase
//import de.flapdoodle.embed.mongo.distribution.Version
//import org.scalatest.BeforeAndAfter
//import org.slf4j.LoggerFactory
//
//
//class MessageApiServiceIntegrationSpec extends AnyFunSpec
//  with OneInstancePerTest
//  with GivenWhenThen
//  with Matchers
//  with MockitoSugar
//  with ArgumentMatchersSugar
//  with BeforeAndAfterAll
//  with BeforeAndAfterEach
//  with TestFixture
//  with MongoEmbedDatabase {
//
//
//  private var client: MongoClient = _
//  private var collection: MongoCollection[Document] = _
//  private var httpClient: Client[Task] = _
//  private var messageApiService: CancelableFuture[ExitCode] = _
//
//  override protected def beforeAll(): Unit = {
//    super.beforeAll()
//
//    val blockingPool = Executors.newFixedThreadPool(1)
//    val blocker = Blocker.liftExecutorService(blockingPool)
//    httpClient = JavaNetClientBuilder[Task](blocker = blocker).create
//
//    client = MongoClient()
//    val DbConnection: MongoDatabase = client.getDatabase("myFirstDatabase")
//    collection = DbConnection.getCollection("test")
//
//    messageApiService = startMessageApiServer.runToFuture
//  }
//
//  override protected def afterAll(): Unit = {
//    client.close()
//    messageApiService.cancel()
//    super.afterAll()
//  }
//
//
//  Feature("Message Service creates message, get message data and delete message") {
//    Scenario("should get message data from ReqRes endpoint and create message in our database") {
//      withMockServer { server =>
//        Given("ReqRes endpoint is online")
//        server.enqueue(
//          new MockResponse().setBody(someValidCreateResponse).setResponseCode(200)
//        )
//
//        When("A valid user message POST request is received")
//        val request = Request[Task](Method.POST, uri"http://localhost:5000/create_messages")
//          .withEntity(someValidCreateMessage)
//        val responseStatus = httpClient.status(request).runSyncUnsafe()
//
//        Then("message is created in our database")
//        server.getRequestCount shouldBe 1
//        responseStatus shouldBe Status.Created
//
//        val actualMessage = collection.find().equals("_id", md5Harsher(someValidMessage)).toString
//        println(actualMessage)
//        actualMessage shouldBe someValidMessage
//
//
//
//      }
//
//    }
//  }
//
//
//
//
//
//
//
//
//  private def startMessageApiServer: Task[ExitCode] = MessageApiServer.run(
//    List(
//      "--dbUrl",
//      "--dbUser",
//      "postgres",
//      "--dbPassword",
//      "postgres"
//    ))
//
//  private def withMockServer(testCode: MockWebServer => Any) = {
//    val server = new MockWebServer
//    server.start(54472)
//    try {
//      testCode(server)
//    } finally {
//      server.shutdown()
//    }
//  }
//
//  private def getTimer(meterName: String) = {
//    val (_, consumedTimer) = MessageApiServer.metricRegistry
//      .getTimers(MetricFilter.contains(meterName))
//      .asScala
//      .headOption
//      .value
//    consumedTimer
//  }
//
//  override protected def beforeEach(): Unit = {
//    MessageApiServer.metricRegistry.removeMatching(MetricFilter.ALL)
//  }
//
//
//}
