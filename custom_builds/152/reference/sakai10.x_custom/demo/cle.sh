#!/bin/sh
#
# Startup script for Tomcat on Linux
# 
# chkconfig: 35 80 20
# description: start & stop tomcat that is running CLE
# --> check

# set this to the location of tomcat 
TOMCAT_HOME=@@TOMCAT_HOME@@
TOMCAT_USER="tomcat"

TOMCAT_PIDFILE=$TOMCAT_HOME/cle.pid
TOMCAT_START_SCRIPT=$TOMCAT_HOME/bin/startup.sh
TOMCAT_STOP_SCRIPT=$TOMCAT_HOME/bin/shutdown.sh
TOMCAT_PID=99999
if [ -e $TOMCAT_PIDFILE ]; then
   TOMCAT_PID=`cat $TOMCAT_PIDFILE`
fi
. $TOMCAT_HOME/bin/setenv.sh
case "$1" in
 start)
    if [ -f $TOMCAT_PIDFILE ]; then
       if [ -e /proc/$TOMCAT_PID ]; then
          is_running=`cat /proc/$TOMCAT_PID/cmdline|grep java`
          if [ $is_running ]; then
             $0 stop
          fi
       fi
       /bin/rm -f $TOMCAT_PIDFILE
    fi
              
    echo "Starting Tomcat"
    su -m -c "$TOMCAT_START_SCRIPT start" $TOMCAT_USER
    ;;
 stop)
    echo "Stopping Tomcat [takes about a minute]..."
    su -m -c "$TOMCAT_STOP_SCRIPT" $TOMCAT_USER
    sleep 30
    if [ -f $TOMCAT_PIDFILE ]; then
       kill -9 $TOMCAT_PID
       /bin/rm -f $TOMCAT_PIDFILE
    fi
    echo "   ...done"
    ;;
 restart)
    $0 stop
    $0 start
    ;;
 *)
    echo "Usage: $0 {start|stop|restart}"
    exit 1
esac
exit 0
