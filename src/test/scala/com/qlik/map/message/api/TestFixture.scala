package com.qlik.map.message.api

import com.qlik.map.message.api.MessageApiUtil.{isWordPalindrome, md5Harsher}
import com.qlik.map.message.messageApiService._

import org.mongodb.scala.Document

trait TestFixture {
  val someValidDocument = Seq(Document("_id" -> md5Harsher("madam"),
    "message" -> "madam",
    "is_word_palindrome" -> isWordPalindrome("madam")),
    Document("_id" -> md5Harsher("rotator"),
      "message" -> "rotator",
      "is_word_palindrome" -> isWordPalindrome("rotator")),
    Document("_id" -> md5Harsher("redder"),
      "message" -> "redder",
      "is_word_palindrome" -> isWordPalindrome("redder")),
    Document("_id" -> md5Harsher("tenet"),
      "message" -> "tenet",
      "is_word_palindrome" -> isWordPalindrome("tenet")))

  val aValidDocument = Document("_id" -> md5Harsher("madam"))
  val someValidMessage = "palindrome"
  val someNotValidMessage = "palindrome"
  val someValidCreateMessage = s"""[{"message": "${someValidMessage}"}]"""
  val someValidCreateResponse = s""" {"response": "created_word: $someValidMessage, is_word_palindrome: false"}"""
  val someNotValidCreateResponse = s""" {"response": "created_word: $someNotValidMessage, is_word_palindrome: false"}"""
}
