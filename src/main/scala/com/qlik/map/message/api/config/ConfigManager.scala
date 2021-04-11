package com.qlik.map.message.api.config

import scala.util.Properties
import com.qlik.map.message.api.database.{CollectionName, DbConnectionParams, DbName, DbPassword, DbUrl, DbUrlSuffix, DbUsername}
import com.qlik.map.message.api.server.ServerConfig
import com.typesafe.config.{Config, ConfigFactory}


/**
 * configManager returns a singleton Object of type DeploymentParam, which combines the server settings and database configuration setting together
 * */

case class DeploymentParam (DbParam: DbConnectionParams, serverParam: ServerConfig)

class ConfigManager(fileNameOption: Option[String] = None) {

  val config: Config = fileNameOption.fold(
    ifEmpty = ConfigFactory.load() )(
    file => ConfigFactory.load(file) )

  def envOrElseConfig(name: String): String = {
    Properties.envOrElse(
      name.toUpperCase.replaceAll("""\.""", "_"),
      config.getString(name)
    )
  }
}

object ConfigManager {

  def apply (configLocation: Option[String]): DeploymentParam = {

    val myConfig = new ConfigManager(configLocation)
    val mongodbUrl = myConfig.envOrElseConfig("db.mongodbUrl")
    val urlPrefix = myConfig.envOrElseConfig("db.urlPrefix")
    val username = myConfig.envOrElseConfig("db.username")
    val password = myConfig.envOrElseConfig("db.password")
    val dbname = myConfig.envOrElseConfig("db.dbname")
    val collectionName = myConfig.envOrElseConfig("db.collectionName")

    val serverHost = myConfig.envOrElseConfig("server.host")
    val port = myConfig.envOrElseConfig("server.port").toInt

    DeploymentParam (DbConnectionParams(
      DbUrl(mongodbUrl),
      DbUrlSuffix(urlPrefix),
      DbUsername(username),
      DbPassword(password),
      DbName(dbname),
      CollectionName(collectionName)
    ),
      ServerConfig(serverHost, port)
    )

  }

}







