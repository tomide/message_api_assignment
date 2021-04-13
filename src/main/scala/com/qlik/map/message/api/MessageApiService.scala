package com.qlik.map.message.api

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, md5Harsher}
import com.qlik.map.message.api.database.DbQuery._
import com.qlik.map.message.messageApiService._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.{Document, MongoCollection}

trait MessageApiService {
  def createMessage(userMessage: Seq[createRequest]): Task[Seq[feedBack]]

  def retrieveMessage(userMessage: Seq[retrieveRequest]): Task[Seq[feedBack]]

  def updateMessage(userMessage: Seq[updateRequest]): Task[Seq[feedBack]]

  def deleteMessage(userMessage: deleteRequest): Task[feedBack]

}


object MessageApiService {
  def apply(implicit ev: MessageApiService): MessageApiService = ev
}

class MessageApiServiceIO(collection: MongoCollection[Document]) extends MessageApiService with StrictLogging {

  override def createMessage(req: Seq[createRequest]): Task[Seq[feedBack]] = {

    val validDocumentEither = for {
      res <- req.map(d => processDocument(createDocument(d, collection)))
    } yield res

    Task.now(validDocumentEither)
  }

  override def retrieveMessage(req: Seq[retrieveRequest]): Task[Seq[feedBack]] = {

    val result = req.map { r =>
      val returnedDocument = collection.find(equal("_id", r.messageId))
        .projection(fields(include("message", "is_word_palindrome"))).first()

      val resultEither = retrieveCommand(returnedDocument)

      resultEither match {
        case Right(d) =>
          if (d.nonEmpty && req.size > 1) {
            Task.now(feedBack(s"message : ${d.head._1}, is_word_palindrome : ${d.head._2}"))
          }
          else if (d.nonEmpty && req.size == 1) {
            Task.raiseError(NoRecordFound(s"no record found for messageId :- ${r.messageId}"))
          }
          else {
            Task.now(feedBack(s"message : ${r.messageId}, no record found"))
          }
        case Left(e) => Task.raiseError(MessageSavingError("MongoDb Database Issues"))
      }
    }
    Task.sequence(result)
  }

  override def updateMessage(req: Seq[updateRequest]): Task[Seq[feedBack]] = {

    val result = req.map { r => {
      val oldDocument = collection.find(equal("_id", md5Harsher(r.oldMessage)))
        .projection(fields(include("message", "is_word_palindrome"))).first()

      val oldResultEither = retrieveCommand(oldDocument)

      val response = oldResultEither match {
        case Right(value) =>
          if (value.nonEmpty) {
            val updateDocument = Document("$set" -> Document("message" -> r.newMessage.toLowerCase(),
              "is_word_palindrome" -> isWordPalindrome(compressMessage(r.newMessage))))
            val updateObservable = collection.updateOne(Filters.eq("_id", md5Harsher(r.oldMessage.toLowerCase())), updateDocument)
            val resultEither = for {
              result <- updateCommand(updateObservable)
            } yield result
            resultEither match {
              case Right(c) => Task.now(feedBack(s"messageId :- ${r.oldMessage} has been updated with new_message :- ${r.newMessage}"))
              case Left(e) => Task.now(feedBack(s"error_message : Db failed during Update"))
            }
          }
          else {
            Task.now(feedBack(s"old_messages :- ${r.oldMessage} was added to the database "))
          }
      }
      response
    }
    }

    Task.sequence(result)

  }

  override def deleteMessage(req: deleteRequest): Task[feedBack] = {

    val oldDocument = collection.find(equal("_id", req.message))
      .projection(fields(include("message", "is_word_palindrome"))).first()

    val ResultEither = retrieveCommand(oldDocument)

    val response = ResultEither match {
      case Right(value) =>
        if (value.nonEmpty) {
          val document = Document("_id" -> req.message)

          val deleteObservable = collection.deleteMany(document)
          val resultEither = for {
            result <- deleteCommand(deleteObservable)
          } yield result
          resultEither match {
            case Right(c) => Task.now(feedBack(s"message: ${req.message} was deleted"))
            case Left(e) => Task.now(feedBack(s"message : 'error during delete process"))
          }
        }
        else {
          Task.raiseError(NoRecordFound(s"messageId : not Found"))
        }
    }

    response

  }

}