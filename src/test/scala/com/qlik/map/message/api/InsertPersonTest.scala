package com.qlik.map.message.api

import com.mongodb.{DBObject, Mongo}
import com.mongodb.casbah.{MongoCollection, MongoConnection, MongoDB}
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import com.github.simplyscala.MongodProps
import com.github.simplyscala.MongoEmbedDatabase
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.funsuite.AnyFunSuite

class InsertPersonTest extends AnyFunSuite with BeforeAndAfter with MongoEmbedDatabase {

  object DBConnector {
    // Connect to default - localhost, 27017
    val mongoConn: MongoConnection = MongoConnection()
    //Create a testDB
    val mongoDB: MongoDB = mongoConn("testDB")
    //returns a collection
    def fetchCollection(collectionName: String): MongoCollection = {
      mongoDB(collectionName)
    }
  }

  class MongoCRUD {

    /**
     * Inserts a person into mongoDB
     */
    def insertPerson(person: DBObject) = {
      val collection = DBConnector.fetchCollection("testData")
      collection.insert(person)
    }
    def findPerson(person: DBObject) = {
      val collection = DBConnector.fetchCollection("testData")
      collection.find(person)
    }
  }



  //declares a variable which will hold the reference to running mongoDB Instance
  var mongoInstance: MongodProps = null
  // Start In-memory Mongo instance in before statement
  before {
    try{ mongoInstance = mongoStart(27017) } //Try starting mongo on this default port
    catch { case ex:Exception => } // Handle exception In case local mongo is running
  }

  //Stop mongo Instance After Running test Case
  after {
    mongoStop(mongoInstance)
  }

  val mongoCRUD = new MongoCRUD

  test("Should be able to insert person Object into MongoDB"){
    val person = MongoDBObject("name"->"Manish")
    val queryResult = mongoCRUD.insertPerson(person)
    //assert if the document was inserted into database
    println(mongoCRUD.findPerson(person).toList)
    assert(mongoCRUD.findPerson(person).count === 1)
  }
}