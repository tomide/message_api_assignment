package com.qlik.map.message.api.database

import java.util.Calendar

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.messageApiService.{InvalidWordError, createRequest, feedBack, retrieveResponse}
import monix.eval.Task
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala.{Completed, Document, Observer, SingleObservable}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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


  def retrieveCommand(returnedDocument: SingleObservable[Document]): Future[retrieveResponse] = {

        val listMapScores = returnedDocument.map(doc => (doc("message").asString.getValue,
          doc("is_word_palindrome").asBoolean().getValue)).collect().toFuture()
        for {
          result <- listMapScores.map(contentSequence => {
            val contentTuple = contentSequence.head
            retrieveResponse(contentTuple._1, contentTuple._2)
          })
        } yield result
  }


  def updateCommand(insertObservable: SingleObservable[UpdateResult]): Either[Throwable, UpdateResult] = {

    val f: Future[UpdateResult] = insertObservable.toFuture()

    val result: Try[UpdateResult] = Await.ready(f, Duration.Inf).value.get

    val resultEither = result match {
      case Success(t) => {
        Right(t)
      }
      case Failure(e) => {
        Left(e)
      }
    }
    resultEither
  }

  def deleteCommand(insertObservable: SingleObservable[Completed]): Either[Throwable, Completed] = {

    val f: Future[Completed] = insertObservable.toFuture()

    val result: Try[Completed] = Await.ready(f, Duration.Inf).value.get

    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }






}
