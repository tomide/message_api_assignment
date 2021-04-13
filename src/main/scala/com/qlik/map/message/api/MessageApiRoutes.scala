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
 1. create message route :  GET / :-
this route accepts a post request 
example:-  [{ "message" : "<valid_message_1>" }, { "message" : "<invalid_message_2>" }, , { "message" : "<a_duplicate_message>" }] // this s list of create requests
response:- [{ "response": "_id: modified_md5_hash_used_in_mongodb_id, message: <valid_message_1> , status: created}
     { "response": message: <invalid_message_2> , status: invalid message format },
     {"response": "message: <a_duplicate_message> status: failed with error : <a_long_error_mongodb> }]}
 */
/**
  2. retrieve message route:  POST => / list_message  :-
this route accepts a post request: 
example:- [{ "messageId" : "<valid_message_id>" }, { "messageId" : "<valid_message_id>" }, { "messageId" : "<nonExisting_message_id>" }] // list of messages to retrieve
response :- [{ "response": "message : ayayass, is_word_palindrome : <Boolean>" },
     { "response": "message : ayayass, is_word_palindrome : <Boolean>"  },
     { "response": "message :<nonExisting_message_id>, no record found" }]
 */
/**
  3. delete message route:  DELETE => / <your_message_id> :-
this route accepts a delete request, with a messageId at the end of the request api
     a. if an existing messageId is received, this end point should return :- { "response": "message: <your_message_id> was deleted" }
     b. if non existing message is received, this endpoint should return :- { "message": "No record found for message :- messageId : not Found" }
 */
/**
   4. update message route: PUT /  :-
example :- [{
    "oldMessage" : <old_message_1> , "newMessage" : <new_message_1>
},
{
    "oldMessage" : <old_message_2> , "newMessage" : <new_message_2>
},
{
    "oldMessage" : <non_existing_old_message> , "newMessage" : <new_message_2>
}
]
reponse :- [
    {
        "response": "old_messages:- <old_message_1> as been updated with new_message <new_message_1>"
    },
    {
        "response": "old_messages:- "<old_message_2> as been updated with new_message <new_message_1>"
    },
    {
        "response": message: <non_existing_old_message> was added to the database "
    }
]
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

      case req @ POST -> Root / "list_message_id" =>
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
