package com.qlik.map.message.api.database
import monix.eval.Task
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}


trait Database

object MongoDbDatabase extends Database {
   def getMongoDatabase (connectionParams: DbConnectionParams): Task[MongoCollection[Document]] = {
    val uri: String = connectionParams.dbUrlSuffix.value +
                      connectionParams.userName.value + ":" +
                      connectionParams.password.value +
                      connectionParams.dbUrl.value
    System.setProperty("org.mongodb.async.type", "netty")
    val client: MongoClient = MongoClient(uri)
    val DbConnection: MongoDatabase = client.getDatabase(connectionParams.dbName.value)
    Task.now(DbConnection.getCollection(connectionParams.collectionName.value))
  }
}


