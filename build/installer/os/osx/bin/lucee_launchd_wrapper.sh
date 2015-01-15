#!/bin/bash

function shutdown()
{
	date
	echo "Shutting down Lucee Tomcat"
	cd $CATALINA_HOME/bin
	./catalina.sh stop
}       

date
echo "Starting Lucee Tomcat"
export CATALINA_PID=/tmp/$$

cd $CATALINA_HOME/bin
./catalina.sh start

# Allow signal which would kill a process to stop
trap shutdown HUP INT QUIT ABRT KILL ALRM TERM TSTP

echo "Waiting for 'cat $CATALINA_PID'"
#wait 'cat $CATALINA_PID'
