package com.qlik.map.message.api

import java.util.Calendar

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.api.database.DbQuery.{insertCommand}
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
  override def createMessage(req: createRequest): Task[feedBack] = {
    val compressedMessage = compressMessage(req.message)
    if (isWordValid(compressedMessage)) {
      val _id = md5Harsher(req.message)
      val message = req.message
      val palindrome = isWordPalindrome(compressedMessage)
      val document = Seq(Document("_id" -> _id, "message" -> message, "is_word_palindrome" -> palindrome))
      val insertObservable = collection.insertMany(document)

      val resultEither = for {
        result <- insertCommand(insertObservable)
      } yield result

      resultEither match {
        case Right(c) => Task.now(feedBack(s"created_message: $message, is_word_palindrome: ${palindrome.toString}"))
        case Left(e) => Task.raiseError(MessageSavingError("message already exist in database"))
      }
    }
    else Task.raiseError(InvalidWordError)
  }

  override def retrieveMessage(req: retrieveRequest): Task[retrieveResponse] = {
    val returnedDocument = collection.find(equal("_id", md5Harsher(req.message)))
      .projection(fields(include("message", "is_word_palindrome"))).first()
    val listMapScores = returnedDocument.map(doc => (doc("message").asString.getValue,
      doc("is_word_palindrome").asBoolean().getValue)).collect().toFuture()
    Task.fromFuture(for {
      result <- listMapScores.map(contentSequence => {
        val contentTuple = contentSequence.head
        logger.info(s""" message : ${req.message} was retrieved from database   -- ${Calendar.getInstance().getTime}""")
        retrieveResponse(contentTuple._1, contentTuple._2)
      })
    } yield result)
  }

  override def updateMessage(req: updateRequest): Task[feedBack] = {
    val updateDocument = Document("$set" -> Document("message" -> req.newMessage))
    val updateObservable = collection.updateOne(Filters.eq("_id", md5Harsher(req.oldMessage)), updateDocument)
    var status : Boolean = false
    updateObservable.subscribe(new Observer[UpdateResult] {
      override def onNext(result: UpdateResult): Unit = logger.info(s""" message - ${req.oldMessage} was updated with ${req.newMessage} -- ${Calendar.getInstance().getTime}""")
      override def onError(e: Throwable): Unit = logger.error(s" onError:  $e   -- ${Calendar.getInstance().getTime} ") ; status = true
      override def onComplete(): Unit = logger.info(s"**** completed transmission on requested ${req.oldMessage} -- ${Calendar.getInstance().getTime}")
    })
    if (status) {
      status = false
      Task.now(feedBack(s"message : '${req.oldMessage}' not in database"))
    } else {
      Task.now(feedBack(s"old_message:- ${req.oldMessage} has been updated with new_message :- ${req.newMessage}"))
    }
  }

  override def deleteMessage(req: deleteRequest): Task[feedBack] = {
    val document = Document("_id" -> md5Harsher(req.message))
    val deleteObservable = collection.deleteMany(document)
    var status : Boolean = false
    deleteObservable.subscribe(observer = new Observer[DeleteResult] {
      override def onNext(result: DeleteResult): Unit = logger.info(s""" message - ${req.message} was deleted  -- ${Calendar.getInstance().getTime}""")
      override def onError(e: Throwable): Unit = logger.error(s" onError:  $e   -- ${Calendar.getInstance().getTime} ") ; status = true
      override def onComplete(): Unit = logger.info(s"**** completed transmission on requested ${req.message} -- ${Calendar.getInstance().getTime}")
    })
    if (status) {
      status = false
      Task.now(feedBack(s"message : '${req.message}' not in database"))
    } else {
      Task.now(feedBack(s"message: ${req.message} was deleted"))
    }
  }

  override def listAllMessages(): Task[Seq[feedBack]] = {
    val observable = collection.find().map(x => x.toJson()).toFuture()
    Task.fromFuture(
      for {
        possibleResult <- observable.map(contentSequence => contentSequence
          .map(x => feedBack(x.replaceAll("\"", ""))))
        result <- if (possibleResult.nonEmpty) Future(possibleResult) else Future(Seq(feedBack("no item in database")))
        _ <- Future(logger.info(s"**** list message request was processed at :-- ${Calendar.getInstance().getTime}"))
      } yield result)
  }

}