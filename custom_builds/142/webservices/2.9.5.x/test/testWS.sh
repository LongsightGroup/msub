#!/bin/sh
# cd target
# jarfiles=`find 2.6.0RC1-SNAPSHOT -iname *.jar | sed 's/$/:/' | perl -pe 'chomp;'`
# cd ..
debug_opt='-Ddebuggin=10000 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5115'

java -jar $debug_opt -Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m -DlinktoolSalt=$1 -DsakaiWSHome=$2 target/sakai-axis-test-2.6.0RC1-SNAPSHOT-jar-with-dependencies.jar 


