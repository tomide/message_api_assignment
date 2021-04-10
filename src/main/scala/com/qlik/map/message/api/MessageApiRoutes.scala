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
import com.qlik.map.message.messageApiService.{InvalidWordError, NoRecordFound, ResponseError, createRequest, deleteRequest, feedBack, retrieveRequest, updateRequest}

/**
- Create, retrieve, update, and delete a message
  - List messages
 */

object MessageApiRoutes extends StrictLogging {
  private val dsl = new Http4sDsl[Task] {}

  import dsl._
  import org.http4s.Method.GET

  def apply(implicit messageService: MessageApiService): HttpRoutes[Task] =
    HttpRoutes.of[Task] {

      case req @ POST -> Root / "create_messages" =>
        (for {
          decodedReq <- req.as[createRequest]
          createdMessage <- messageService.createMessage(decodedReq)
          result <- Created(createdMessage)
        } yield result).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      case GET -> Root / "message" / word =>
        (for {
          requestedWord <- Task.now(retrieveRequest(word))
          messageRequested <- messageService.retrieveMessage(requestedWord)
          result <- Ok(messageRequested)
        } yield result).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      case req @ PUT -> Root / word =>
        (for {
          newWord <- req.as[createRequest]
          decodedReq <- Task.now(updateRequest(word, newWord.word))
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