#!/bin/bash -e

REMOTE_M2REPO='http://download.scala-ide.org/sbt-integration/m2repo'
LOCAL_IVY=~/.ivy2/local

SBTRC_VERSION=$(grep '<sbtrc.version>' pom.xml | awk -F '[<>]' '{print $3;}')


#
# $1 - group id
# $2 - artifact id
# $3 - version
# $4 - extra suffix for remote m2
# $5 - extra path section for local ivy2
function fetchAndStore () {

  LOCAL_FOLDER="${LOCAL_IVY}/$1/$2/$5$3/jars"
  mkdir -p "${LOCAL_FOLDER}"

  wget -O "${LOCAL_FOLDER}/$2.jar" "${REMOTE_M2REPO}/${1//\.//}/$2$4/$3/$2-$3.jar"

}


fetchAndStore 'com.typesafe.sbtrc' 'server-0-13' "${SBTRC_VERSION}"
fetchAndStore 'com.typesafe.sbtrc' 'client' "${SBTRC_VERSION}"
fetchAndStore 'com.typesafe.sbtrc' 'terminal' "${SBTRC_VERSION}"
# TODO: make the extra parameter less static
fetchAndStore 'com.typesafe.sbtrc' 'ui-interface-0-13' "${SBTRC_VERSION}" '_2.10_0.13' '/scala_2.10/sbt_0.13/'


