package com.qlik.map.message.api.database

import com.qlik.map.message.messageApiService.retrieveResponse
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, Document, Observable, SingleObservable}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await}
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

  def retrieveCommand(returnedDocument: SingleObservable[Document]): Future[retrieveResponse] = {

    //update api for mongodb collection does not return error if document does not exist
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


}
