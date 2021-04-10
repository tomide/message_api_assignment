package com.qlik.map.messages.api.database
import monix.eval.Task
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}


trait Database

object Database {

  def getMongoDbConfig ()= ???

  def getMongoDatabase: Task[MongoCollection[Document]] = {
    val uri: String = "mongodb+srv://new-user-101:tF7wdxEb9v5KWUpo@cluster0.l1jnn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority"
    System.setProperty("org.mongodb.async.type", "netty")
    val client: MongoClient = MongoClient(uri)
    val DbConnection: MongoDatabase = client.getDatabase("myFirstDatabase")
    Task.now(DbConnection.getCollection("test"))
  }
}

