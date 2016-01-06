#!/bin/sh
jarfiles=`find target -iname *.jar | sed 's/$/:/' | perl -pe 'chomp;'`
debug_opt='-Ddebuggin=10000 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005'

java -cp $jarfiles $debug_opt -Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m com.rsmart.admin.customizer.app.Main docs/cle-configuration-template.xml ../../ docs/ docs/toolIcons


