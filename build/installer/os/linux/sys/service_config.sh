#!/bin/bash
#
###############################################################################
#
# Purpose:      This script is used to add and remove Linux services for Lucee
#		on both RHEL/CentOS systems and Debian/Linux systems.
#
# Copyright:    Copyright (C) 2012-2013
#               by Jordan Michaels (jordan@viviotech.net)
#
# License:      LGPL 3.0
#               http://www.opensource.org/licenses/lgpl-3.0.html
#
#               This program is distributed in the hope that it will be useful, 
#               but WITHOUT ANY WARRANTY; without even the implied warranty of 
#               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
#               GNU General Public License for more details.
#
# Usage:        Run "configure_service.sh --help" for complete usage info
#
# History:	1.0 - Initial Release
#		1.1 - Added Runlevel 2 to default Ubuntu start
###############################################################################

version=1.1
progname=$(basename $0)
basedir=$( cd "$( dirname "$0" )" && pwd );

# switch the subshell to the basedir so all relative dirs resolve
cd $basedir;

# ensure we're running as root
if [ ! $(id -u) = "0" ]; then
        echo "* [FATAL]: This installation script needs to be run as root.";
        echo "* Exiting...";
        exit 1;
fi

# create the "usage" function to display to improper users
function print_usage {
cat << EOF

Usage: $0 OPTIONS

OPTIONS:
 -v --version		print installer script version
 -h --help		print this help message
 -i --install		enables Lucee to start at boot
 -r --remove		removes the lucee_ctl file from init.d and removes it
 -p --path		path to lucee_ctl (will attempt to find if not specified)

Examples:

To install the lucee_ctl service, you would just use:

    # $0 -i

To remove the service, here's the longer example

    # $0 --remove

If you have the lucee_ctl file in an unusual place or if you're running this
script from an unusual place, you will need to tell the script where to find
the lucee_ctl script. You can do that with:

    # $0 --install --path /path/to/lucee_ctl

Additional help can be found at: http://lucee.org/

EOF
}

function print_version {
cat << EOF

$progname v. $version
Copyright (C) 2012-2013 Jordan Michaels (jordan@viviotech.net)
Licensed under LGPL 3.0
http://www.opensource.org/licenses/lgpl-3.0.html

This is free software.  You may redistribute copies of it under the terms of 
the GNU General Public License <http://www.gnu.org/licenses/gpl.html>. 
There is NO WARRANTY, to the extent permitted by law.

EOF
}

# eval variables passed via command line
SHORTOPTS="hvVirp:"
LONGOPTS="help,version,install,remove,path:"
OPTS=$(getopt -o $SHORTOPTS --long $LONGOPTS -n "$progname" -- "$@")

if [ $? -ne 0 ]; then
        echo "* Type '$progname --help' for usage information" 1>&2
        exit 1
fi

eval set -- "$OPTS"

# declare initial variables (so we can check them later)
myMode=
myPath="../lucee_ctl" # default path to lucee_ctl script

while [ $# -gt 0 ]; do
    case $1 in
        -h|--help)
            print_usage
            exit 0
            ;;
        -v|-V|--version)
            print_version
            exit 0
            ;;
        -i|--install)
            myMode="install"
            shift
            ;;
        -r|--remove)
            myMode="remove"
            shift 2
            ;;
        -p|--path)
            myPath=$2
            shift 2
            ;;
        --)
            shift
            break
            ;;
        *)
            echo "* [FATAL]: Unknown error encounter when processing argument: $1" 1>&2
            exit 1
            ;;
    esac
done

###############################################################################
# BEGIN FUNCTION LIST
###############################################################################

function test_input {
	# test for install or removal command
	if [[ -z $myMode ]]; then
		# no command, nothing to do
		# display help and exit as error
		print_usage;
		exit 1;
	fi
	
	# test to make sure we have a present and executable lucee_ctl script
	# only test for lucee_ctl if we're doing a removal; otherwise not needed
	if [[ $myMode == "install" ]]; then
	if [[ -z $myPath ]] || [[ ! -x $myPath ]]; then
		echo "* [ERROR]: $myPath is not found or not executable.";
	        # not specified or not executable, run autodetect
	        autodetectPath;
	fi
	fi
}

function autodetectPath {
	echo -n "* [INFO] Attempting to autodetect lucee_ctl script location...";
	
	# create a var to track if we found the file or not in our upcoming tests
	local myFileFound=0;

	# if the user is hitting this function, then the default location of
	# "../lucee_ctl" failed. Try the full default location instead:
	local defaultlocation="/opt/lucee/lucee_ctl";

	# now test it
	if [[ ! -f ${defaultLocation} ]] || [[ ! -x ${defaultLocation} ]]; then
		echo "[FAILED]";
		echo "* [ERROR]: NOT found in /opt/lucee/lucee_ctl...";
	else
		# looks good, set the variable
		myPath="/opt/lucee/lucee_ctl";
		local myFileFound=1;
		echo "[SUCCESS]";
	fi
	
	if [[ $myFileFound -eq 0 ]]; then
		echo "* [FATAL]: No lucee_ctl file found and cannot autodetect!";
		echo "* [INFO]: Ensure the lucee_ctl file is present and executable.";
		exit 1;
	fi
}

function getLinuxVersion {
        # this function is thanks to Arun Singh c/o Novell
        local OS=`uname -s`
        local REV=`uname -r`
        local MACH=`uname -m`

        if [ "${OS}" = "SunOS" ] ; then
                local OS=Solaris
                local ARCH=`uname -p`
                local OSSTR="${OS} ${REV}(${ARCH} `uname -v`)"
        elif [ "${OS}" = "AIX" ] ; then
                local OSSTR="${OS} `oslevel` (`oslevel -r`)"
        elif [ "${OS}" = "Linux" ] ; then
                local KERNEL=`uname -r`
                if [ -f /etc/redhat-release ] ; then
                        local DIST='RedHat'
                        local PSUEDONAME=`cat /etc/redhat-release | sed s/.*\(// | sed s/\)//`
                        local REV=`cat /etc/redhat-release | sed s/.*release\ // | sed s/\ .*//`
                elif [ -f /etc/SUSE-release ] ; then
                        local DIST=`cat /etc/SUSE-release | tr "\n" ' '| sed s/VERSION.*//`
                        local REV=`cat /etc/SUSE-release | tr "\n" ' ' | sed s/.*=\ //`
                elif [ -f /etc/mandrake-release ] ; then
                        local DIST='Mandrake'
                        local PSUEDONAME=`cat /etc/mandrake-release | sed s/.*\(// | sed s/\)//`
                        local REV=`cat /etc/mandrake-release | sed s/.*release\ // | sed s/\ .*//`
                elif [ -f /etc/debian_version ] ; then
                        local DIST="Debian `cat /etc/debian_version`"
                        local REV=""
                fi
                if [ -f /etc/UnitedLinux-release ] ; then
                        local DIST="${DIST}[`cat /etc/UnitedLinux-release | tr "\n" ' ' | sed s/VERSION.*//`]"
                fi

                local OSSTR="${OS} ${DIST} ${REV}(${PSUEDONAME} ${KERNEL} ${MACH})"
        fi
        myLinuxVersion=${OSSTR};
}

function detectYumExists {
        echo -n "* [INFO]: Checking for 'yum' executable in the PATH...";
        hash yum &> /dev/null
        if [[ $? -eq 1 ]]; then
                echo "";
                echo "* [FATAL]: 'yum' executable doesn't exist in PATH.";
		echo "* [FATAL]:  Automatic installation of required software impossible.";
                echo "Exiting...";
                exit 1;
        else
                echo "[FOUND]";
        fi
}

function detectAPTExists {
        echo -n "* [INFO]: Checking for 'apt-get' executable in the PATH...";
        hash apt-get &> /dev/null
        if [[ $? -eq 1 ]]; then
                echo "";
                echo "* [FATAL]: 'apt-get' executable doesn't exist in PATH."; 
                echo "* [FATAL]:  Automatic installation of required software impossible.";
                echo "Exiting...";
                exit 1;
        fi
        echo "[FOUND]";
}


function test_chkconfig {
	# tests for the existance of the chkconfig command for setting up
	# services to start at boot on RHEL-based systems
        echo -n "* [INFO]: Checking for 'chkconfig' executable in the PATH...";

        hash chkconfig &> /dev/null
        if [[ $? -eq 1 ]]; then
		echo
                echo "* [ERROR]: 'chkconfig' executable doesn't exist in PATH.";
		
		# didn't find chkconfig, can we install it with yum?
		if [[ -z $yumInstallAttempt ]] || [[ $yumInstallAttempt -eq 0 ]]; then
			
			# set the install attempted variable to "no"
			yumInstallAttempt=0;
			detectYumExists;
			
			# if the above function doesn't fatally error out,
			# attempt to install chkconfig via YUM
			yum -y install chkconfig
		else
			# if we hit this, it means that we attempted to install
			# but it failed
			echo "";
			echo "* [FATAL]: 'chkconfig' executable doesn't exist in PATH."; 
			echo "* [FATAL]:  Automatic installation was attempted but failed.";
			echo "Exiting...";
			exit 1;
		fi
	else
		echo "[FOUND]";
        fi
}

function test_updateRCD {
	# tests for the existance of the update-rc.d command for setting up
	# services to start at boot on Debian-based systems
        echo -n "* [INFO]: Checking for 'update-rc.d' executable in the PATH...";

        hash  &> /dev/null
        if [[ $? -eq 1 ]]; then
                echo
                echo "* [ERROR]: 'update-rc.d' executable doesn't exist in PATH.";

                # didn't find update-rc.d, can we install it with apt?
                if [[ -z $aptInstallAttempt ]] || [[ $aptInstallAttempt -eq 0 ]]; then
                        
                        # set the install attempted variable to "no"
                        aptInstallAttempt=0;
                        detectAPTExists;

                        # if the above function doesn't fatally error out,
                        # attempt to install update-rc.d via apt
			# update-rc.d is provided by the rcconf package
                        apt-get install rcconf
                else
                        # if we hit this, it means that we attempted to install
                        # but it failed
                        echo "";
                        echo "* [FATAL]: 'update-rc.d' executable doesn't exist in PATH.";
                        echo "* [FATAL]:  Automatic installation was attempted but failed.";
                        echo "Exiting...";
                        exit 1;
                fi
        else
                echo "[FOUND]";
        fi
}

function install_luceeCTL {
	# start out by seeing what kind of system we're on
	getLinuxVersion;

	# now see what commands we need to run based on the system type
	if [[ $myLinuxVersion == *RedHat*  ]]; then
		echo "* [INFO]: Detected RedHat-based build.";
		test_chkconfig;
		
		# copy lucee_ctl to /etc/init.d/
                cp $myPath /etc/init.d/lucee_ctl
                chmod 755 /etc/init.d/lucee_ctl

		# install the lucee_ctl service
		chkconfig lucee_ctl on
		
	elif [[ $myLinuxVersion == *Debian*  ]]; then
		echo "* [INFO]: Detected Debian-based build.";
		test_updateRCD;
		
		# copy lucee_ctl to /etc/init.d/
		cp $myPath /etc/init.d/lucee_ctl
		chmod 755 /etc/init.d/lucee_ctl

		# install lucee_ctl service
		update-rc.d lucee_ctl start 10 2 3 4 5 . stop 10 0 .
	fi
}

function remove_luceeCTL {
        # start out by seeing what kind of system we're on
        getLinuxVersion;
	
        # now see what commands we need to run based on the system type
        if [[ $myLinuxVersion == *RedHat*  ]]; then
                echo "* [INFO]: Detected RedHat-based build.";
                test_chkconfig;

                # install the lucee_ctl service
                chkconfig lucee_ctl off

        elif [[ $myLinuxVersion == *Debian*  ]]; then
                echo "* [INFO]: Detected Debian-based build.";
                test_updateRCD;

                # install lucee_ctl service
                update-rc.d -f lucee_ctl remove
        fi
}




###############################################################################
# END FUNCTION LIST
###############################################################################

# start by verifying input
test_input;

# functions will depend on the mode
case $myMode in
        "install")
        # run install mode functions
	# print_version;
	echo "";
	echo "## Installing Lucee Service ##";
	echo "";
	install_luceeCTL
	echo "";
        echo "##  Installation Complete   ##";
	echo "";
        ;;
        "remove")
        # run test mode functions
	# print_version;
	echo "";
        echo "##  Removing Lucee Service  ##";
	echo "";
	remove_luceeCTL;
	echo "";
        echo "## Service Removal Complete ##";
	echo "";
        ;;
esac
