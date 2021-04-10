package com.qlik.map.message.api.database

import monix.eval.Task
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}


trait Database

object Database {

  def getMongoDbConfig ()= ???

  def getMongoDatabase: Task[MongoCollection[Document]] = {
    val uri: String = ""
    System.setProperty("org.mongodb.async.type", "netty")
    val client: MongoClient = MongoClient(uri)
    val DbConnection: MongoDatabase = client.getDatabase("myFirstDatabase")
    Task.now(DbConnection.getCollection("test"))
  }
}

