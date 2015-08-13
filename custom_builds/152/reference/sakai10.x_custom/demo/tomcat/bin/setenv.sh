#!/bin/sh
#export JAVA_HOME=/usr/java
export JAVA_OPTS="-server -Xmx1500m -XX:MaxNewSize=300m -XX:MaxPermSize=500m -Djava.awt.headless=true -Dhttp.agent=Sakai-News-Tool"

# uncomment the following line and enter timezone if server is running in a different one
# some examples for the U.S.:  US/Eastern, US/Central, US/Mountain, US/Pacific, US/Arizona
#export JAVA_OPTS="$JAVA_OPTS -Duser.timezone="

# uncomment the following if using Tomcat v 5.5.27+
export JAVA_OPTS="$JAVA_OPTS -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false"

# for JDK 1.6
export JAVA_OPTS="$JAVA_OPTS -Dsun.lang.ClassLoader.allowArraySyntax=true"

export CATALINA_PID="$CATALINA_HOME/cle.pid"
