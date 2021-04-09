package com.qlik.map.messages

import io.circe.generic.auto._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import monix.eval.Task
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._
import scala.util.control.NoStackTrace



package object apiService {

  /**
  - Create, retrieve, update, and delete a message
  - List messages
  */



  /**
   * Request ADTs
   */
  case class createRequest(word: String) extends AnyVal
  implicit val createMessageDecoder: Decoder[createRequest] = deriveUnwrappedDecoder
  implicit val createMessageEncoder: Encoder[createRequest] = deriveUnwrappedEncoder

  case class retrieveRequest(word: String) extends AnyVal
  implicit val retrieveMessageDecoder: Decoder[retrieveRequest] = deriveUnwrappedDecoder
  implicit val retrieveMessageEncoder: Encoder[retrieveRequest] = deriveUnwrappedEncoder

  case class updateRequest(oldWord: String, newWord: String)
  implicit val updateMessageDecoder: EntityDecoder[Task, updateRequest] = jsonOf[Task, updateRequest]
  implicit val updateMessageEncoder: EntityEncoder[Task, updateRequest] = jsonEncoderOf[Task, updateRequest]

  case class deleteRequest(word: String, reason: String)
  implicit val deleteMessageDecoder: EntityDecoder[Task, deleteRequest] = jsonOf[Task, deleteRequest]
  implicit val userRequestEntityEncoder: EntityEncoder[Task, deleteRequest] = jsonEncoderOf[Task, deleteRequest]

  /**
   * Response ADTs
   */
  case class feedBack(response: String) extends AnyVal
  implicit val feedBackDecoder: Decoder[feedBack] = deriveUnwrappedDecoder
  implicit val feedBackEncoder: Encoder[feedBack] = deriveUnwrappedEncoder

  case class retrieveResponse(word: String, Palindrome: Boolean)
  implicit val retrieveResponseDecoder: EntityDecoder[Task, retrieveResponse] = jsonOf[Task, retrieveResponse]
  implicit val retrieveResponseEncoder: EntityEncoder[Task, retrieveResponse] = jsonEncoderOf[Task, retrieveResponse]

  case class listResponse(messages: Seq[String])
  implicit val listResponseDecoder: EntityDecoder[Task, listResponse] = jsonOf[Task, listResponse]
  implicit val listResponseEncoder: EntityEncoder[Task, listResponse] = jsonEncoderOf[Task, listResponse]


  /**
   * Error ADTs
   */
  sealed trait BackEndApiError extends NoStackTrace {
    /** Provides a message appropriate for logging. */
    def message: String
  }

  case object InvalidWordError extends BackEndApiError {
    override def message: String = "inValid word. Please remove spaces and numbers between the word"
  }

  case class NoRecordFound(word: String) extends BackEndApiError {
    override def message: String = s"No record found for id - $word"
  }

  case class MessageSavingError(errorMessage: String) extends BackEndApiError {
    override def message: String = errorMessage
  }


  case class ResponseError(message: String)
  implicit val responseErrorJsonDecoder: EntityDecoder[Task, ResponseError] = jsonOf[Task, ResponseError]
  implicit def responseErrorDecoder: EntityDecoder[Task, ResponseError] = jsonOf[Task, ResponseError]
  implicit def responseErrorEncoder: EntityEncoder[Task, ResponseError] = jsonEncoderOf[Task, ResponseError]

}
