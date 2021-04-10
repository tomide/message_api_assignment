package com.qlik.map.message.api

import com.qlik.map.message.api.Util.{isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.messageApiService._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, MongoCollection, Observer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MessageApiService {

  def createMessage(userMessage: createRequest): Task[feedBack]

  def retrieveMessage(userMessage: retrieveRequest): Task[retrieveResponse]

  def updateMessage(userMessage: updateRequest): Task[feedBack]

  def deleteMessage(userMessage: deleteRequest): Task[feedBack]

  def listAllMessages(): Task[Seq[feedBack]]

}

object MessageApiService {
  def apply(implicit ev: MessageApiService): MessageApiService = ev
}

class MessageApiServiceIO(collection: MongoCollection[Document]) extends MessageApiService with StrictLogging {


  override def createMessage(userMessage: createRequest): Task[feedBack] = {
    if (isWordValid(userMessage.word)) {

      val document = Seq(Document("_id" -> md5Harsher(userMessage.word),
        "word" -> userMessage.word,
        "Palindrome" -> isWordPalindrome(userMessage.word)))
      val insertObservable = collection.insertMany(document)

      insertObservable.subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println(s"onNext: $result")

        override def onError(e: Throwable): Unit = println(s"onError: $e")

        override def onComplete(): Unit = println("onComplete")
      })
      Task.now(feedBack(s"your message was ${userMessage.word}"))
    }
    else Task.raiseError(InvalidWordError)
  }

  override def retrieveMessage(userMessage: retrieveRequest): Task[retrieveResponse] = {
    val collScores = collection.find(equal("_id", md5Harsher(userMessage.word)))
      .projection(fields(include("word", "Palindrome"))).first()

    val listMapScores = collScores.map(doc => (doc("word").asString.getValue,
      doc("Palindrome").asBoolean().getValue)).collect().toFuture()
    Task.fromFuture(for {
      result <- listMapScores.map(contentSequence => {
        val contentTuple = contentSequence.head
        retrieveResponse(contentTuple._1, contentTuple._2)
      })
    } yield result)
  }

  override def deleteMessage(userMessage: deleteRequest): Task[feedBack] = {
    val document = Document("_id" -> md5Harsher(userMessage.word))
    val deleteObservable = collection.deleteMany(document)
    deleteObservable.subscribe(observer = new Observer[DeleteResult] {
      override def onNext(result: DeleteResult): Unit = {
        logger.info(s""" message - ${userMessage.word} was deleted""")
      }

      override def onError(e: Throwable): Unit = println(s"onError: $e")

      override def onComplete(): Unit = println("onComplete")
    })
    Task.now(feedBack(s"word: ${userMessage.word} was deleted"))
  }

  override def updateMessage(userMessage: updateRequest): Task[feedBack] = {
    val updateDocument = Document("$set" -> Document("word" -> userMessage.newWord))
    val updateObservable = collection
      .updateOne(Filters.eq("_id", md5Harsher(userMessage.oldWord)), updateDocument)
    updateObservable.subscribe(new Observer[UpdateResult] {
      override def onNext(result: UpdateResult): Unit = println(s"onNext: ${result.toString} was deleted")

      override def onError(e: Throwable): Unit = println(s"onError: $e")

      override def onComplete(): Unit = println("onComplete")
    })
    Task.now(feedBack(s"word: ${userMessage.oldWord} has been updated"))
  }

  override def listAllMessages(): Task[Seq[feedBack]] = {
    val observable = collection.find().map(x => x.toJson()).toFuture()
    Task.fromFuture(
      for {
        possibleResult <- observable.map(contentSequence => contentSequence
          .map(x => feedBack(x.replaceAll("\"", ""))))
        result <- if (possibleResult.nonEmpty) Future(possibleResult) else Future(Seq(feedBack("no item in database")))
      } yield result)
  }

}