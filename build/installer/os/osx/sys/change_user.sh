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
elif [[ ! $1 =~ ^[a-z_][a-zA-Z0-9_-]+$ ]]; then  # make sure username is a valid format
        echo "Error: Invalid User Name";
	echo "";
	echo "Rules for User Names:";
	echo "1) User Names must start with a lower-case letter or underscore"
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
	myControlScriptName="/Library/LaunchDaemons/org.apache.tomcat.plist";



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

function getmyGroupID {
	echo -n "Getting an available group ID below 500...";
	#list, sort, and only grab something under 500 	
	myGroupID=`dscl . -list /Groups PrimaryGroupID | sort -nr -k 2 | grep -oE '[^5 ][0-8]{2,}$' | head -1`;
	myGroupID=$(($myGroupID + 1));
	echo "[GROUPID $myGroupID]";
}

function getmyUserID {
	echo -n "Using the same ID for user if available...";
	if [ `dscl . -list /Users UniqueID | sort -nr -k 2 | grep -c ${myGroupID}` -gt 0 ]; then
		echo "User ID ${myGroupID} already exists, cannot use same group ID, finding next available...";
		myUserID=`dscl . -list /Users UniqueID | sort -nr -k 2 | grep -oE '[^5 ][0-8]{2,}$' | head -1`;
		myUserID=$(($myUserID + 1));
	else
		myUserID=${myGroupID};
	fi
	echo "[USERID $myUserID]";
}

# function to create the new user
function createUserAndGroup {
	echo "Initializing user and group creation process...";
	checkUserExists;
	checkGroupExists;
	if [ ${myGroupNeedsCreating} -eq 1 ]; then
		getmyGroupID;
        echo -n "Creating Group...";
			sudo dscl . -create /Groups/${myUserName} PrimaryGroupID ${myGroupID};
			sudo dscl . -create /Groups/${myUserName} RealName "Tomcat Administrator Group";
			sudo dscl . -create /Groups/${myUserName} Password \*;
		echo "[DONE]";
        fi
	if [ ${myUserNeedsCreating} -eq 1 ]; then
		getmyUserID;
		echo -n "Creating User...";
			sudo dscl . -create /Users/${myUserName} UniqueID ${myUserID};
			sudo dscl . -create /Users/${myUserName} PrimaryGroupID ${myGroupID};
			#sudo dscl . -create /Users/${myUserName} HomeDirectory ${myInstallDir};
			sudo dscl . -create /Users/${myUserName} HomeDirectory /var/empty;
			sudo dscl . -create /Users/${myUserName} UserShell /usr/bin/false;
			sudo dscl . -create /Users/${myUserName} RealName "Tomcat Administrator";
			sudo dscl . -create /Users/${myUserName} Password \*;
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
	        cp ${myControlScriptName} ${myInstallDir}/${myControlScriptName}.old;
	fi
}
        

#####################
# Run function list #
#####################

createUserAndGroup;
rebuildControlScript;
updateInstallDir;
