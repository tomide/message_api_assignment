#!/bin/bash
# This is the master script that acts as the ENTRYPOINT for docker.
#set -x

#Run the application
java -cp messageApiService.jar -Xmx1G com.qlik.map.message.api.MessageApiServer
