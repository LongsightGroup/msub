#!/bin/sh
#
#
## 1. Update TOMCAT_HOME to point to your CLE25 Tomcat installation
## 2. Update JDBC driver & path below

TOMCAT_HOME=/opt/cle/25

CLASSPATH="$CLASSPATH:$TOMCAT_HOME/common/lib/log4j-1.2.8.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/common/lib/sakai-util-log-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/common/lib/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/components/sakai-content-pack/WEB-INF/lib/sakai-content-impl-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/components/sakai-content-pack/WEB-INF/lib/sakai-db-conversion-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/components/sakai-content-pack/WEB-INF/lib/sakai-db-storage-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/components/sakai-content-pack/WEB-INF/lib/sakai-entity-util-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/components/sakai-content-pack/WEB-INF/lib/sakai-util-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/commons-collections-3.2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/sakai-component-api-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/sakai-content-api-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/sakai-db-api-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/sakai-entity-api-M2.jar"
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/shared/lib/sakai-util-api-M2.jar"


##### JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$TOMCAT_HOME/common/lib/mysql-connector-java-5.1.5-bin.jar"
## ORACLE ##
#CLASSPATH="$CLASSPATH:$TOMCAT_HOME/common/lib/ojdbc-14.jar"

# for debugging, uncomment next line
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
