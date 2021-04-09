package com.qlik.map.messages.api

package object database {

  sealed trait AuthMethod

  case class Login(username: String, password: String) extends AuthMethod

  case class ServiceConf(host: String,
                         retryWrites: Boolean,
                         databaseName: String,
                         authMethods: List[AuthMethod]
                        )
}
