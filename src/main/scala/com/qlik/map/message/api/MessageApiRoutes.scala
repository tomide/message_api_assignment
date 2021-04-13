package com.qlik.map.message.api

import com.mongodb.MongoBulkWriteException
import com.qlik.map.message.messageApiService.{retrieveRequest, updateRequest}
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import monix.eval.Task
import org.http4s.{HttpRoutes, Method}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.mongodb.scala.{MongoBulkWriteException, MongoException}
/**
- This MessageApiRoutes object holds the routes used by the server.
 there are 5 routes currently implemented;
 */
/**
(NB: A valid message is a message that does contain any numerical values)
 */
/**
 1. create message route GET / :-
this route accepts a post request in the following format [{ "message" : "<valid_message>" }]
     a. if valid message is received, this enpoint should return :- [{ "response": "created_word: <your_message>, is_word_palindrome: <Boolean>" }]
     b. if valid message is received and message already exist in database, this endpoint should return :- { "response": "message: '<your_message>' already exist, is_word_palindrome: <Boolean>" }
 */
/**
  2. retrieve message route  => /  :-
this route accepts a post request in the following format [{ "message" : "<valid_message>" }]
     a. if existing message is received, this end point should return :- [{ "response": "message: <your_message>, is_word_palindrome: <Boolean>" }]
     b. if non existing message is received, this endpoint should return :- [{ "message: <your_message> not in database" }]
 */
/**
  3. delete message route => / <your_message_id> :-
this route accepts a delete request, with a message at the end of the request api
     a. if existing message is received, this end point should return :- { "response": "message: <your_message> was deleted" }
     b. if non existing message is received, this endpoint should return :- { "message": "No record found for message :- messageId : not Found" }
 */
/**
   4. update message route /  :-
this route accepts a pur request in the following format [{ "old_message" : "<old_valid_message>" , "new_message" : "<new_valid_message>" }]
     a. if existing message is received, this end point should return :- { "response": "old_message: <old_message> has been updated with new message :- <new_message_in_body>" }
     b. if non existing message is received, this endpoint should return :- { "old_message: <old_your_message> was added to the database " }
 */


object MessageApiRoutes extends StrictLogging {

  import com.qlik.map.message.messageApiService.{InvalidWordError, MessageSavingError, NoRecordFound, ResponseError, createRequest, deleteRequest, retrieveRequest, updateRequest}


  private val dsl = new Http4sDsl[Task] {}

  import dsl._
  import org.http4s.Method.GET

  def apply(implicit messageService: MessageApiService): HttpRoutes[Task] =
    HttpRoutes.of[Task] {
      case req @ POST -> Root  =>
        (for {
          decodedReq <- req.as[Seq[createRequest]]
          createdMessage <- messageService.createMessage(decodedReq)
          result <- Created(createdMessage)
        } yield result).onErrorHandleWith {
          case e : MessageSavingError =>  NotAcceptable(ResponseError(e.message).asJson)
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case e : MongoException => ExpectationFailed(ResponseError("error_message : mongodb connection failed / issue in mongoDb server").asJson)
          case _ => BadRequest(ResponseError("error_message : Bad request / Decoder - Encoder Failed").asJson)
        }

      case req @ POST -> Root / "list_message" =>
        (for {
          decodedReq <- req.as[Seq[retrieveRequest]]
          messageRequested <- messageService.retrieveMessage(decodedReq)
          result <- Ok(messageRequested)
        } yield result).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case InvalidWordError =>  NotAcceptable(ResponseError(InvalidWordError.message).asJson)
          case e: MongoException =>  ExpectationFailed(ResponseError(e.toString).asJson)
          case _ => BadRequest(ResponseError("badly formed request. please check api documentation").asJson)
        }

      //added word as endpoint parameter just to show that they can been combined.
      case req @ PUT -> Root =>
        (for {
          decodedReq <- req.as[Seq[updateRequest]]
          allObservables <- messageService.updateMessage(decodedReq)
          allMessages <- Ok(allObservables)
        } yield allMessages).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case e: InvalidWordError.type =>  NotAcceptable(ResponseError(e.message).asJson)
          case e: MongoException =>  ExpectationFailed(ResponseError(e.toString).asJson)
          case _ => BadRequest(ResponseError("error_message : badly formed request. please check api documentation").asJson)
        }

      case DELETE -> Root / messageId =>
        (for {
          allObservables <- messageService.deleteMessage(deleteRequest(messageId))
          allMessages <- Ok(allObservables)
        } yield allMessages).onErrorHandleWith {
          case e: NoRecordFound =>  NotFound(ResponseError(e.message).asJson)
          case e: InvalidWordError.type =>  NotAcceptable(ResponseError(e.message).asJson)
          case e: NoSuchElementException => NotFound(ResponseError(s" error_message: message :- $messageId not in database").asJson)
          case _ => BadRequest(ResponseError("error_message : badly formed request. please check api documentation").asJson)
        }


    }
}