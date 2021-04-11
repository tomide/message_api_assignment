package com.qlik.map.message.api

import org.http4s.HttpRoutes
import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, Encoder}
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import monix.eval.Task
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import com.qlik.map.message.messageApiService.{InvalidWordError, MessageSavingError, NoRecordFound, ResponseError, createRequest, deleteRequest, feedBack, retrieveRequest, updateRequest}

/**
- This MessageApiRoutes object holds the routes used by the server.
 there are 5 routes currently implemented;
 1. create_message :-

this is route accepts a post request in the following format { "message" : "<valid_message>" }
(NB: A valid message is a message that does contain any numerical values) and returns a response base on the following conditions :
     a. if valid message is received, this enpoint should return :- { "response": "created_word: <your_message>, is_word_palindrome: <Boolean>" }
      b. if invalid message is received,this endpoint should return :- {
 */

object MessageApiRoutes extends StrictLogging {
  private val dsl = new Http4sDsl[Task] {}

  import dsl._
  import org.http4s.Method.GET

  def apply(implicit messageService: MessageApiService): HttpRoutes[Task] =
    HttpRoutes.of[Task] {

      case req @ POST -> Root / "create_message" =>
        (for {
          decodedReq <- req.as[createRequest]
          createdMessage <- messageService.createMessage(decodedReq)
          result <- Created(createdMessage)
        } yield result).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case e: MessageSavingError =>  AlreadyReported(ResponseError(e.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      case GET -> Root / "message" / message =>
        (for {
          requestedMessage <- Task.now(retrieveRequest(message))
          messageRequested <- messageService.retrieveMessage(requestedMessage)
          result <- Ok(messageRequested)
        } yield result).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case e: NoSuchElementException => NotFound(ResponseError("i fdey mad").asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      case req @ PUT -> Root / word =>
        (for {
          newWord <- req.as[createRequest]
          decodedReq <- Task.now(updateRequest(word, newWord.message))
          allObservables <- messageService.updateMessage(decodedReq)
          allMessages <- Ok(allObservables)
        } yield allMessages).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case e: InvalidWordError.type =>  NotAcceptable(ResponseError(e.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      case GET -> Root / "list_messages" =>
        (for {
          allObservables <- messageService.listAllMessages()
          allMessages <- Ok(allObservables)
        } yield allMessages).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case e: InvalidWordError.type =>  NotAcceptable(ResponseError(e.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)

        }

      case req @ DELETE -> Root =>
        (for {
          decodedReq <- req.as[deleteRequest]
          allObservables <- messageService.deleteMessage(decodedReq)
          allMessages <- Ok(allObservables)
        } yield allMessages).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case e: InvalidWordError.type =>  NotAcceptable(ResponseError(e.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }


    }
}