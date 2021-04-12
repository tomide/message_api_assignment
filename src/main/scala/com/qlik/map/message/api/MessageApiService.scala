package com.qlik.map.message.api

import java.util.Calendar

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.api.database.DbQuery.{deleteCommand, insertCommand, retrieveCommand, updateCommand}
import com.qlik.map.message.messageApiService._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, MongoCollection, Observer, SingleObservable}
import monix.execution.Scheduler.Implicits.global

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

    Task.fromFuture(retrieveCommand(returnedDocument))

  }

  override def updateMessage(req: updateRequest): Task[feedBack] = {

    val updateDocument = Document("$set" -> Document("message" -> req.newMessage))
    val updateObservable = collection.updateOne(Filters.eq("_id", md5Harsher(req.oldMessage)), updateDocument)

    val resultEither = for {
      result <- updateCommand(updateObservable)
    } yield result

    resultEither match {
      case Right(c) => Task.now(feedBack(s"old_message:- ${req.oldMessage} has been updated with new_message :- ${req.newMessage}"))
      case Left(e) => Task.now(feedBack(s"message : '${req.oldMessage}' not in database"))
    }

  }

  override def deleteMessage(req: deleteRequest): Task[feedBack] = {
    val document = Document("_id" -> md5Harsher(req.message))
    val deleteObservable = collection.deleteMany(document)

    val resultEither = for {
      result <- deleteCommand(deleteObservable)
    } yield result

    resultEither match {
      case Right(c) => Task.now(feedBack(s"message: ${req.message} was deleted"))
      case Left(e) => Task.now(feedBack(s"message : 'error during delete process"))
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