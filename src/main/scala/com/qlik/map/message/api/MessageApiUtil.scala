package com.qlik.map.message.api

import java.security.MessageDigest

/**
 * Util object carries all functions needed to validate messages or functions need to perform various activities on a message
 * */

object MessageApiUtil {

  def md5Harsher(message: String): String = {
    val hash = MessageDigest.getInstance("MD5").digest(message.getBytes).mkString("")
    if (hash(0).toString == "-") hash.slice(1, hash.length) else hash
  }
  def isWordValid(message: String): Boolean = if (!message.exists(_.isDigit)) true else false
  def isWordPalindrome(message: String): Boolean = if (message.reverse == message) true else false
  def compressMessage(message: String): String = message.replaceAll("[-+.^:,]","")

}
