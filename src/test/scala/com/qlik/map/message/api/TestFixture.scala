package com.qlik.map.message.api

import com.qlik.map.messages.api.Util.{isWordPalindrome, md5Harsher}
import com.qlik.map.messages.apiService.feedBack
import org.mongodb.scala.Document

trait TestFixture {
  val someValidDocument = Seq(Document("_id" -> md5Harsher("Madam"),
    "word" -> "Madam",
    "Palindrome" -> isWordPalindrome("Madam")),
    Document("_id" -> md5Harsher("Rotator"),
      "word" -> "Rotator",
      "Palindrome" -> isWordPalindrome("Rotator")),
    Document("_id" -> md5Harsher("Redder"),
      "word" -> "Redder",
      "Palindrome" -> isWordPalindrome("Redder")),
    Document("_id" -> md5Harsher("Tenet"),
      "word" -> "Tenet",
      "Palindrome" -> isWordPalindrome("Tenet")))

  val someValidMessage = "Palindrome"

  val someValidCreateMessage = s"""{"word": "${someValidMessage}"}"""

  val someValidCreateResponse = s""" {"response": "word: sure boy has been updated"}"""


}
