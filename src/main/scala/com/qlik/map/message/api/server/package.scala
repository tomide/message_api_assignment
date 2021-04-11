package com.qlik.map.message.api


/**
 * package object used to store data types used for connecting to server host system or environment.
 * a can be modified to have multiple servers and ports
 * */

package object server {
  case class ServerConfig( host: String,
                           port: Int
                         )
}
