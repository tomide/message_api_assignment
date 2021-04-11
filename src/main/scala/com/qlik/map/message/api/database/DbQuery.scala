package com.qlik.map.message.api.database

import java.util.Calendar

import com.qlik.map.message.api.MessageApiUtil.{compressMessage, isWordPalindrome, isWordValid, md5Harsher}
import com.qlik.map.message.messageApiService.{InvalidWordError, createRequest, feedBack}
import monix.eval.Task
import org.mongodb.scala.{Completed, Document, Observer, SingleObservable}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

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


  def updateCommand(insertObservable: SingleObservable[Completed]): Either[Throwable, Completed] = {

    val f: Future[Completed] = insertObservable.toFuture()

    val result: Try[Completed] = Await.ready(f, Duration.Inf).value.get

    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
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
