package com.qlik.map.message.api

import java.security.MessageDigest

object Util {

  def md5Harsher(s: String): String = {
    val hash = MessageDigest.getInstance("MD5").digest(s.getBytes).mkString("")
    if (hash(0).toString == "-")  hash.slice(1, hash.length) else hash
  }

  def isWordValid(word: String): Boolean = if (word.trim.split(" ").length == 1 && !word.exists(_.isDigit)) true else false
  def isWordPalindrome(word: String): Boolean = if (word.reverse == word) true else false

}
