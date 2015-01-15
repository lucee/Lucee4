#!/bin/bash
#
# ----------------------------------------------------------------------------------
# Purpose:	Changes the user that the CFML server runs as and creates custom
#		control script based on that user.
# Author:	Jordan Michaels (jordan@viviotech.net)
# Copyright:	Jordan Michaels, 2010-2012, All rights reserved.
#
# Usage: 	change_user.sh [username] [install dir] [engine] [nobackup]
#
#		[username] must start with a lower-case letter and must be alpha-
#		numeric
#		[engine] must be "lucee". Engine name is used
#		in the control script name, such as "lucee_ctl"
# ----------------------------------------------------------------------------------
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
# OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# ----------------------------------------------------------------------------------

if [ ! $(id -u) = "0" ]; then
        echo "Error: This script needs to be run as root.";
        echo "Exiting...";
        exit;
fi

# test user input

if [ -z $1 ]; then  # make sure it was specified
        echo "Error: No User Name Specified.";
	echo "";
        echo "Usage: ./change_user.sh [username] /path/to/installdir [engine]";
        exit 1;
elif [[ ! $1 =~ ^[a-z][a-zA-Z0-9_-]+$ ]]; then  # make sure username is a valid format
        echo "Error: Invalid User Name";
	echo "";
	echo "Rules for User Names:";
	echo "1) User Names must start with a lower-case letter"
	echo "2) User Names must contain only alphanumeric characters, hyphens, or underscores.";
	echo "";
        echo "Usage: ./change_user.sh [username] /path/to/installdir [engine]";
        exit 1;
else
        myUserName=$1;
fi

if [ -z $2 ]; then  # make sure install dir is specified
        echo "Error: No Installation Directory Specified.";
	echo "";
        echo "Usage: ./change_user.sh [username] /path/to/installdir [engine]";
        exit 1;
elif [ ! -d $2 ]; then  # make sure it's a directory
	echo "Error: Directory provided does not exist or is not a directory.";
	echo "";
       	echo "Usage: ./change_user.sh [username] /path/to/installdir [engine]";
        exit 1;
elif [ ! -d "$2/tomcat/" ]; then  # make sure it contains tomcat
        echo "Error: Directory provided doesn't appear to be valid.";
	echo "";
        echo "Usage: ./change_user.sh [username] /path/to/installdir [engine]";
        exit 1;
else
       	myInstallDir=$2;
fi
myCFServerName="Lucee";
myControlScriptName="lucee_ctl";



if [ -z $4 ]; then # check to see if we're making a backup of the control scropt
	echo "I will backup control scripts to .old";
	myControlNeedsBackup=1;
elif [ "$4" = "nobackup" ]; then
	echo "I will not backup control scripts.";
	myControlNeedsBackup=0;
else
	echo "I will backup control scripts to .old";
	myControlNeedsBackup=1;
fi

###################
# begin functions #
###################

# check to see if the user exists
function checkUserExists {
	echo -n "Checking to see if user exists...";
	myUserNeedsCreating=0;
	if [ `cat /etc/passwd | grep -c ${myUserName}:` -gt 0 ]; then
		echo "[FOUND]";
	else
		echo "[NOT FOUND]";
		myUserNeedsCreating=1;
	fi
}

function checkGroupExists {
	echo -n "Checking to see if group exists...";
	myGroupNeedsCreating=0;
	if [ `cat /etc/group | grep -c ${myUserName}:` -gt 0 ]; then
		echo "[FOUND]";
	else
		echo "[NOT FOUND]";
		myGroupNeedsCreating=1;
	fi
}

# function to create the new user
function createUserAndGroup {
	echo "Initializing user and group creation process...";
	checkUserExists;
	checkGroupExists;
	if [ ${myGroupNeedsCreating} -eq 1 ]; then
                echo -n "Creating Group...";
                groupadd ${myUserName} -r;
		echo "[DONE]";
        fi
	if [ ${myUserNeedsCreating} -eq 1 ]; then
		echo -n "Creating User...";
		useradd ${myUserName} -g ${myUserName} -d ${myInstallDir} -s /bin/false -r;
		echo "[DONE]";
	fi
}

function updateInstallDir {
	echo -n "Applying new permissions to Installation Directory...";
	chown -R ${myUserName}:${myUserName} ${myInstallDir};
	echo "[DONE]";
}

function rebuildControlScript {
	echo "Rebuilding Control Scripts for new User...";
	# backup current control script
        if [ ${myControlNeedsBackup} -eq 1 ]; then
                # If we're backing up, do it
                mv ${myInstallDir}/${myControlScriptName} ${myInstallDir}/${myControlScriptName}.old;
        else
                # otherwise, just remove the old file
                rm -rf ${myInstallDir}/${myControlScriptName}
        fi
	# write new one...
	singleQuote=$'\140'
	TomcatControlScript="${myInstallDir}/${myControlScriptName}";
	TEMP=`touch $TomcatControlScript`;
	TEMP=`echo "#!/bin/bash" >> $TomcatControlScript`;
        TEMP=`echo "# chkconfig: 345 22 78" >> $TomcatControlScript`;
        TEMP=`echo "# description: Tomcat/${myCFServerName} Control Script" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "### BEGIN INIT INFO" >> $TomcatControlScript`;
        TEMP=`echo "# Provides:          lucee_ctl" >> $TomcatControlScript`;
        TEMP=`echo "# Required-Start:    \\$network " >> $TomcatControlScript`;
        TEMP=`echo "# Required-Stop:     \\$network " >> $TomcatControlScript`;
        TEMP=`echo "# Default-Start:     2 3 4 5" >> $TomcatControlScript`;
        TEMP=`echo "# Default-Stop:      0" >> $TomcatControlScript`;
        TEMP=`echo "# Short-Description: Tomcat/Lucee Control Script" >> $TomcatControlScript`;
        TEMP=`echo "# Description:       This is the control script that starts and stops Tomcat which contains a global install of the Lucee servlet." >> $TomcatControlScript`;
        TEMP=`echo "### END INIT INFO" >> $TomcatControlScript`;
	TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "# switch the subshell to the tomcat directory so that any relative" >> $TomcatControlScript`;
        TEMP=`echo "# paths specified in any configs are interpreted from this directory." >> $TomcatControlScript`;
        TEMP=`echo "cd ${myInstallDir}/tomcat/" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "# set base params for subshell" >> $TomcatControlScript`;
        TEMP=`echo "CATALINA_BASE=${myInstallDir}/tomcat; export CATALINA_BASE" >> $TomcatControlScript`;
        TEMP=`echo "CATALINA_HOME=${myInstallDir}/tomcat; export CATALINA_HOME" >> $TomcatControlScript`;
        TEMP=`echo "CATALINA_PID=${myInstallDir}/tomcat/work/tomcat.pid; export CATALINA_PID" >> $TomcatControlScript`;
        TEMP=`echo "CATALINA_TMPDIR=${myInstallDir}/tomcat/temp; export CATALINA_TMPDIR" >> $TomcatControlScript`;
        TEMP=`echo "JRE_HOME=${myInstallDir}/jdk/jre; export JRE_HOME" >> $TomcatControlScript`;
        TEMP=`echo "JAVA_HOME=${myInstallDir}/jdk; export JAVA_HOME" >> $TomcatControlScript`;
	TEMP=`echo "TOMCAT_OWNER=${myUserName}; export TOMCAT_OWNER" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "findpid() {" >> $TomcatControlScript`;
        TEMP=`echo "	PID_FOUND=0" >> $TomcatControlScript`;
        TEMP=`echo "	if [ -f \\"\\$CATALINA_PID\\" ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "                PIDNUMBER=${singleQuote}cat \\"\\$CATALINA_PID\\"${singleQuote}" >> $TomcatControlScript`;
        TEMP=`echo "                TEST_RUNNING=${singleQuote}ps -p \\${PIDNUMBER} | grep \\${PIDNUMBER} | grep java${singleQuote}" >> $TomcatControlScript`;
        TEMP=`echo "	        if [ -z \\"\\${TEST_RUNNING}\\" ]" >> $TomcatControlScript`;
        TEMP=`echo "        	        then" >> $TomcatControlScript`;
        TEMP=`echo "                	        # echo \\"PID file exists but PID [\\$PIDNUMBER] is not running... removing PID file [\\$CATALINA_PID]\\"" >> $TomcatControlScript`;
        TEMP=`echo "	                        rm -rf \\$CATALINA_PID" >> $TomcatControlScript`;
        TEMP=`echo "	        else" >> $TomcatControlScript`;
        TEMP=`echo "			# PID is found and running" >> $TomcatControlScript`;
        TEMP=`echo "			PID_FOUND=1" >> $TomcatControlScript`;
        TEMP=`echo "		fi" >> $TomcatControlScript`;
        TEMP=`echo "	fi" >> $TomcatControlScript`;
        TEMP=`echo "}" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "start() {" >> $TomcatControlScript`;
        TEMP=`echo "        echo -n \\" * Starting ${myCFServerName}: \\"" >> $TomcatControlScript`;
        TEMP=`echo "        findpid" >> $TomcatControlScript`;
        TEMP=`echo "	# only actually run the start command if the PID isn't found" >> $TomcatControlScript`;
        TEMP=`echo "	if [ \\$PID_FOUND -eq 0 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "		su -p -s /bin/sh \\$TOMCAT_OWNER \\$CATALINA_HOME/bin/startup.sh" >> $TomcatControlScript`;
        TEMP=`echo "		COUNT=0" >> $TomcatControlScript`;
        TEMP=`echo "		while [ \\$COUNT -lt 3 ] ; do" >> $TomcatControlScript`;
        TEMP=`echo "			COUNT=\\$((\\$COUNT+1))" >> $TomcatControlScript`;
        TEMP=`echo "			echo -n \\". \\"" >> $TomcatControlScript`;
        TEMP=`echo "			sleep 1" >> $TomcatControlScript`;
        TEMP=`echo "		done" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"[DONE]\\"" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"It may take a few moments for ${myCFServerName} to start processing\\"" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"CFML templates. This is normal.\\"" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "	else" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"[ALREADY RUNNING]\\"" >> $TomcatControlScript`;
        TEMP=`echo "	fi" >> $TomcatControlScript`;
        TEMP=`echo "}" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "stop() {" >> $TomcatControlScript`;
        TEMP=`echo "        echo -n \\" * Shutting down ${myCFServerName}: \\"" >> $TomcatControlScript`;
        TEMP=`echo "	findpid" >> $TomcatControlScript`;
        TEMP=`echo "	if [ \\$PID_FOUND -eq 1 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "        	su -p -s /bin/sh \\$TOMCAT_OWNER \\$CATALINA_HOME/bin/shutdown.sh" >> $TomcatControlScript`;
        TEMP=`echo "		COUNT=0" >> $TomcatControlScript`;
        TEMP=`echo "        	while [ \\$PID_FOUND -eq 1 ] ; do" >> $TomcatControlScript`;
        TEMP=`echo "			findpid" >> $TomcatControlScript`;
        TEMP=`echo "			COUNT=\\$((\\$COUNT+1))" >> $TomcatControlScript`;
        TEMP=`echo "			if [ \\$COUNT -gt 20 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "				break" >> $TomcatControlScript`;
        TEMP=`echo "			fi" >> $TomcatControlScript`;
        TEMP=`echo "			echo -n \\". \\"" >> $TomcatControlScript`;
        TEMP=`echo "			# pause while we wait to try again" >> $TomcatControlScript`;
        TEMP=`echo "			sleep 1" >> $TomcatControlScript`;
        TEMP=`echo "		done" >> $TomcatControlScript`;
        TEMP=`echo "		findpid" >> $TomcatControlScript`;
        TEMP=`echo "		if [ \\$PID_FOUND -eq 1 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "			echo \\"[FAIL]\\"" >> $TomcatControlScript`;
        TEMP=`echo "			echo \\" * The Tomcat/${myCFServerName} process is not responding. Forcing shutdown...\\"" >> $TomcatControlScript`;
        TEMP=`echo "			forcequit" >> $TomcatControlScript`;
        TEMP=`echo "		else" >> $TomcatControlScript`;
        TEMP=`echo "			echo \\"[DONE]\\"" >> $TomcatControlScript`;
        TEMP=`echo "		fi" >> $TomcatControlScript`;
        TEMP=`echo "	elif [ ! -f \\$CATALINA_PID ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "		# if the pid file doesn't exist, just say \\"okay\\"" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"[DONE]\\"" >> $TomcatControlScript`;
        TEMP=`echo "	else" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"[Cannot locate Tomcat PID (${singleQuote}cat \\$CATALINA_PID${singleQuote}) ]\\"" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"If the Tomcat process is still running, either kill the\\"" >> $TomcatControlScript`;
        TEMP=`echo "       	echo \\"PID directly or use the 'killall' command.\\"" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"IE: # killall java\\"" >> $TomcatControlScript`;
        TEMP=`echo "        	echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "	fi" >> $TomcatControlScript`;
        TEMP=`echo "}" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "forcequit() {" >> $TomcatControlScript`;
        TEMP=`echo "        echo -n \\" * Forcing ${myCFServerName} Shutdown: \\"" >> $TomcatControlScript`;
        TEMP=`echo "	findpid" >> $TomcatControlScript`;
        TEMP=`echo "	if [ \\$PID_FOUND -eq 1 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "		# if the PID is still running, force it to die" >> $TomcatControlScript`;
        TEMP=`echo "	        # su -p -s /bin/sh \\$TOMCAT_OWNER $CATALINA_HOME/bin/shutdown.sh -force" >> $TomcatControlScript`;
        TEMP=`echo "		kill -9 \\$PIDNUMBER" >> $TomcatControlScript`;
        TEMP=`echo "		rm -rf \\$CATALINA_PID" >> $TomcatControlScript`;
        TEMP=`echo "	        echo \\"[DONE]\\"" >> $TomcatControlScript`;
        TEMP=`echo "	else" >> $TomcatControlScript`;
        TEMP=`echo "		# there is no PID, tell the user." >> $TomcatControlScript`;
        TEMP=`echo "		echo \\"[FAIL]\\"" >> $TomcatControlScript`;
        TEMP=`echo "                echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "                echo \\"No Tomcat PID found. If the Tomcat process is still\\"" >> $TomcatControlScript`;
        TEMP=`echo "                echo \\"active under a different PID, please kill it manually.\\"" >> $TomcatControlScript`;
        TEMP=`echo "                echo \\"--------------------------------------------------------\\"" >> $TomcatControlScript`;
        TEMP=`echo "	fi" >> $TomcatControlScript`;
        TEMP=`echo "}" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "status() {" >> $TomcatControlScript`;
        TEMP=`echo "	findpid" >> $TomcatControlScript`;
        TEMP=`echo "	if [ \\$PID_FOUND -eq 1 ] ; then" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\" * ${myCFServerName}/Tomcat is running (PID: \\$PIDNUMBER)\\"" >> $TomcatControlScript`;
        TEMP=`echo "	else" >> $TomcatControlScript`;
        TEMP=`echo "		echo \\" * PID not found.\\"" >> $TomcatControlScript`;
        TEMP=`echo "	fi" >> $TomcatControlScript`;
        TEMP=`echo "}" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "case \\"\\$1\\" in" >> $TomcatControlScript`;
        TEMP=`echo "  start)" >> $TomcatControlScript`;
        TEMP=`echo "        start" >> $TomcatControlScript`;
        TEMP=`echo "        ;;" >> $TomcatControlScript`;
        TEMP=`echo "  stop)" >> $TomcatControlScript`;
        TEMP=`echo "        stop" >> $TomcatControlScript`;
        TEMP=`echo "        ;;" >> $TomcatControlScript`;
        TEMP=`echo "  forcequit)" >> $TomcatControlScript`;
        TEMP=`echo "	forcequit" >> $TomcatControlScript`;
        TEMP=`echo "	;;" >> $TomcatControlScript`;
        TEMP=`echo "  restart)" >> $TomcatControlScript`;
        TEMP=`echo "        stop" >> $TomcatControlScript`;
        TEMP=`echo "        sleep 5" >> $TomcatControlScript`;
        TEMP=`echo "        start" >> $TomcatControlScript`;
        TEMP=`echo "        ;;" >> $TomcatControlScript`;
        TEMP=`echo "  status)" >> $TomcatControlScript`;
        TEMP=`echo "	status" >> $TomcatControlScript`;
        TEMP=`echo "	;;" >> $TomcatControlScript`;
        TEMP=`echo "  *)" >> $TomcatControlScript`;
        TEMP=`echo "        echo \\" * Usage: \\$0 {start|stop|restart|forcequit|status}\\"" >> $TomcatControlScript`;
        TEMP=`echo "        exit 1" >> $TomcatControlScript`;
        TEMP=`echo "        ;;" >> $TomcatControlScript`;
        TEMP=`echo "esac" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
        TEMP=`echo "exit 0" >> $TomcatControlScript`;
        TEMP=`echo "" >> $TomcatControlScript`;
	
	# make it executable
	chmod 744 $TomcatControlScript;	

	# see if there's a control script in the init directory
	if [ -f /etc/init.d/${myControlScriptName} ]; then
                # if there is, copy the new control script over it
		cp -f $TomcatControlScript /etc/init.d/${myControlScriptName};
	fi
}
        

#####################
# Run function list #
#####################

createUserAndGroup;
rebuildControlScript;
updateInstallDir;
