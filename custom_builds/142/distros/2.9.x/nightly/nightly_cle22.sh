#!/bin/bash

# rSmart CLE 2.2 nightly build script.
#
# 2006, E. Souhrada, The rSmart Group
# --------------------------------------------------------

export JAVA_HOME=/usr/java/
MAVEN_HOME=/usr/maven/
ANT_HOME=/usr/ant/
PATH=$PATH:$MAVEN_HOME/bin:$JAVA_HOME/bin:$ANT_HOME/bin

curDateTime=`date +%m.%d.%Y\ %H:%M:%S`
echo "starting run at $curDateTime" >> /home/rsmart/bin/nightly_log

NIGHTLY_HOME=/opt/nightly
HOME=/home/rsmart
NIGHTLY_SAKAI_HOME=$NIGHTLY_HOME/sakai-demo
BUILD=$NIGHTLY_HOME/nightly-build

echo "Shutting down any running tomcats ..."
cd $NIGHTLY_SAKAI_HOME
$NIGHTLY_SAKAI_HOME/stop-sakai.sh
sleep 20

echo "Clearing out build directory ..."
rm -rf $BUILD
mkdir -p $BUILD
cd $BUILD
#
echo "Obtaining sakai source and overlaying CLE ..."
svn co http://svn.rsmart.com/svn/rsmart-cle/trunk/sakai
mv sakai sakai-22

echo "Building CLE 2.2 ..."
cd $BUILD/sakai-22
cd reference/demo/

REPO_REV=`svn info|grep Revision`
echo "version.sakai=$REPO_REV built $curDateTime" >> nightly.properties

cd $BUILD/sakai-22
maven pack-demo

echo "Deploying to $NIGHTLY_HOME ..."
cd $NIGHTLY_HOME
rm -rf $NIGHTLY_SAKAI_HOME
tar xfz $BUILD/sakai-22/sakai-demo.tar.gz

ssh root@vm-mysql4 /root/bin/runDB.sh

cd $NIGHTLY_SAKAI_HOME/bin

./startup.sh
echo "Nightly Build and Deploy Complete at `date`"
echo "Server should be up in approximately 3 minutes."
echo

curDateTime=`date +%m.%d.%Y\ %H:%M:%S`
echo "ending run at $curDateTime" >> /home/rsmart/bin/nightly_log

