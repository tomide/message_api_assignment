package com.qlik.map.message.api

package object database {

  case class DbUrl(value: String) extends AnyVal

  case class DbUrlSuffix(value: String) extends AnyVal

  case class DbUsername(value: String) extends AnyVal

  case class DbPassword(value: String) extends AnyVal

  case class DbName(value: String) extends AnyVal

  case class CollectionName(value: String) extends AnyVal

  case class DbConnectionParams(dbUrl: DbUrl,
                              dbUrlSuffix: DbUrlSuffix,
                              userName: DbUsername,
                              password: DbPassword,
                              dbName: DbName,
                              collectionName: CollectionName)
}
