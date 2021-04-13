package com.qlik.map.message.api.database

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.messageApiService.{createRequest, feedBack, retrieveResponse}
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, MongoCollection, Observable, SingleObservable}

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 *
 * The DbQueryHandler is responsible for all interaction with database engine and also responsible for converting all future result to an either of Result or Error
 * This Object can be easily be abstracted to a generic type, because the signature of the methods are all the same :- f : A => E[Error, B]
 * */

object DbQuery {

 def insertCommand(insertObservable: SingleObservable[Completed]): Either[Throwable, Completed] = {

   val f: Future[Completed] = insertObservable.toFuture()
   val result: Try[Completed] = Await.ready(f, Duration.Inf).value.get
   val resultEither = result match {
     case Success(t) => Right(t)
     case Failure(e) => Left(e)
   }
   resultEither
 }

  def retrieveCommand(returnedDocument: SingleObservable[Document]): Either[Throwable, Seq[(String, Boolean)]] = {

    val f  = returnedDocument.map(doc => (doc("message").asString.getValue,
          doc("is_word_palindrome").asBoolean().getValue)).collect().toFuture()

    val result: Try[Seq[(String, Boolean)]] = Await.ready(f, Duration.Inf).value.get

    val resultEither = result match {

      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither

  }

  def updateCommand(insertObservable: SingleObservable[UpdateResult]): Either[Throwable, UpdateResult] = {

    val f: Future[UpdateResult] = insertObservable.toFuture()
    val result: Try[UpdateResult] = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def deleteCommand(deleteObservable: SingleObservable[DeleteResult]): Either[Throwable, DeleteResult] = {

    val f: Future[DeleteResult] = deleteObservable.toFuture()
    val result: Try[DeleteResult] = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def getAllMessageCommand(observable: Observable[String]): Either[Throwable, Seq[String]] = {

    val f: Future[Seq[String]] = observable.toFuture()
    val result: Try[Seq[String]] = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def createDocument(cm: createRequest, collection: MongoCollection[Document]): Either[String, Map[String,SingleObservable[Completed]]] = {
    val compressedMessage = compressMessage(cm.message.toLowerCase())
    if (isWordValid(compressedMessage)) {
      val _id = md5Harsher(cm.message)
      val message = cm.message.toLowerCase()
      val palindrome = isWordPalindrome(compressedMessage)
      val document = Seq(Document("_id" -> _id, "message" -> message, "is_word_palindrome" -> palindrome))
      val insertObservable = collection.insertMany(document)
      Right(Map(_id + "_" + cm.message -> insertObservable))
    }
    else Left(cm.message)
  }

  def processDocument(newReq: Either[String, Map[String,SingleObservable[Completed]]]) : feedBack = {

    newReq match {
      case Right(c) => {
        insertCommand(c.values.head) match {
          case Right(_) => feedBack(s"_id : ${c.keys.head.split("_").head} , message: ${c.keys.head.split("_").tail.head}, status: created")
          case Left(e) => feedBack(s"message ${c.keys.head.split("_").tail.head} status: failed with error : $e")
        }
      }
      case Left(e) => feedBack(s"message: $e status : invalid message format")
    }
  }

}
