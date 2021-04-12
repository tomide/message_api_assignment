package com.qlik.map.message.api

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.api.database.DbQuery._
import com.qlik.map.message.messageApiService._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.{Document, MongoCollection}

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
    val compressedMessage = compressMessage(req.message.toLowerCase())
    if (isWordValid(compressedMessage)) {
      val _id = md5Harsher(req.message)
      val message = req.message.toLowerCase()
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

    val returnedDocument = collection.find(equal("_id", md5Harsher(req.message.toLowerCase())))
      .projection(fields(include("message", "is_word_palindrome"))).first()
    Task.fromFuture(retrieveCommand(returnedDocument))

  }

  override def updateMessage(req: updateRequest): Task[feedBack] = {

    val updateDocument = Document("$set" -> Document("message" -> req.newMessage.toLowerCase()))
    val updateObservable = collection.updateOne(Filters.eq("_id", md5Harsher(req.oldMessage.toLowerCase())), updateDocument)
    val resultEither = for {
      result <- updateCommand(updateObservable)
    } yield result
    resultEither match {
      case Right(c) => Task.now(feedBack(s"old_message:- ${req.oldMessage} has been updated with new_message :- ${req.newMessage}"))
      case Left(e) => Task.now(feedBack(s"message : 'error during update process"))
    }

  }

  override def deleteMessage(req: deleteRequest): Task[feedBack] = {
    val document = Document("_id" -> md5Harsher(req.message.toLowerCase()))
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
    val observable = collection.find().map(x => x.toJson())
    val resultEither = for {
      result <- getAllMessageCommand(observable)
    } yield result
    resultEither match {
      case Right(c) => if (c.nonEmpty) Task.now(c.map (d => feedBack(d.replaceAll("\"", "")))) else Task.now(Seq(feedBack("no item in database")))
      case Left(e) => Task.now(Seq(feedBack(s"message : 'error during retrieval process")))
    }

  }

}