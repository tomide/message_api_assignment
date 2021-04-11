package com.qlik.map.message.api.database
import monix.eval.Task
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}


/**
 * Database is generic trait and can be easily implemented to have more methods
 *
 * getMongoDBCollection methods returns collection from mangoDb object. this is the collection where all the messages will be stored
 * */


trait Database

object MongoDbDatabase extends Database {
   def getMongoDBCollection (connectionParams: DbConnectionParams): Task[MongoCollection[Document]] = {
    val uri: String = connectionParams.dbUrlSuffix.value +
                      connectionParams.userName.value + ":" +
                      connectionParams.password.value +
                      connectionParams.dbUrl.value
    System.setProperty("org.mongodb.async.type", "netty")
    val client: MongoClient = MongoClient(uri)
    val DbConnection: MongoDatabase = client.getDatabase(connectionParams.dbName.value)
     val collection : MongoCollection[Document] = DbConnection.getCollection(connectionParams.collectionName.value)
    Task.now(collection)
  }
}


