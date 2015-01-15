#!/bin/bash
###############################################################################
# Purpose: 	install and configure mod_cfml in Apache
# Author: 	Jordan Michaels (jordan@viviotech.net)
# License:	LGPL 3.0
# 		http://www.opensource.org/licenses/lgpl-3.0.html
#
# Usage:	install_mod_cfml.sh
#		-m [install|test]
#			install: will perform tests and install if all tests
#				pass.
#			test: will perform tests to ensure system meets
#				requirements.
#		-l /path/to/cfml/server
#			The home directory of the CFML server. IE: /opt/lucee
#		-f /path/to/apache.conf
#			Full system path to Apache config file.
#			IE: /etc/apache/apache2.conf
#		-c /path/to/apachectl
#			Full system path to Apache Control Script.
#			IE: /usr/sbin/apachectl
###############################################################################

if [ ! $(id -u) = "0" ]; then
        echo "Error: This script needs to be run as root.";
        echo "Exiting...";
        exit;
fi

# create the "usage" function to display to improper users
function usage {
cat << EOF
usage: $0 -m [install|test] OPTIONS

OPTIONS:
   -l	/path/to/cfml/server	: The home directory of the CFML server. IE: /opt/lucee
   -f	/path/to/apache.conf	: Full system path to Apache config file.
				  IE: /etc/apache/apache2.conf (debian/ubuntu)
				  IE: /etc/httpd/conf/httpd.conf (redhat/centos)
   -c	/path/to/apachectl	: Full system path to Apache Control Script.
				  IE: /usr/sbin/apachectl

EOF
}

# declare initial variables (so we can check them later)
myMode=
myCFMLHome=
myApacheConf=
myApacheCTL=
installModPerlHit=

# parse command-line params
while getopts “hm:l:f:c:” OPTION
do
     case $OPTION in
	 h)
	     usage
	     exit 1
	     ;;
         m)
             myMode=$OPTARG
             ;;
         l)
             myCFMLHome=$OPTARG
             ;;
         f)
             myApacheConf=$OPTARG
             ;;
         c)
             myApacheCTL=$OPTARG
             ;;
         ?)
             usage
	     exit
             ;;
     esac
done

function verifyInput {
# verify myMode
if [[ -z $myMode ]] && [[ $myMode != "install" ]] && [[ $myMode != "test" ]]; then
	# mode isn't set to a proper mode
	usage;
	exit 1;
else
	if [[ $myMode = "install" ]]; then
		# if we're install mode, make sure we have install vars
		if [[ -z $myCFMLHome ]]; then
			autodetectCFMLHome;
		fi
		
		if [[ -z $myApacheConf ]]; then
			audodetectApacheConf;
                fi
	fi # close install mode checks
fi # close mode check

# verify myApacheCTL
if [[ -z $myApacheCTL ]] || [[ ! -x $myApacheCTL ]]; then
        # apachectl is needed for testing and install, try to autodetect
        autodetectApacheCTL;
fi
}

###################
# begin functions #
###################

function getLinuxVersion {
	# this function is thanks to Arun Singh c/o Novell
	local OS=`uname -s`
	local REV=`uname -r`
	local MACH=`uname -m`

	if [ "${OS}" = "SunOS" ] ; then
		local OS=Solaris
		local ARCH=`uname -p`	
		local OSSTR="${OS} ${REV}(${ARCH} `uname -v`)"
	elif [ "${OS}" = "Darwin" ] ; then
		local OSSTR="${OS}"
		local DIST='Darwin'
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

function autodetectApacheCTL {
        # this function will be called if the $myApacheCTL variable is blank
        # and can be expanded upon as different OS's are tried and as OS's evolve.
	
	echo -n "ApacheCTL undefined, autodetecting OS...";
	
        # use the getLinuxVersion function to try and see if we know what we're being run on
	# GetLinuxVersion will return myLinuxVersion
        getLinuxVersion;

	if [[ $myLinuxVersion == *RedHat*  ]] || [[ $myLinuxVersion == *Debian*  ]]; then
		# RedHat and Debian keep the apachectl file in the same place usually,
		# and will also cover CentOS, Ubuntu, and Mint.
		
		echo "[SUCCESS]";
		echo -n "Checking default location of ApacheCTL...";

		local ctlFileFound=0;

		# test the default location
		local defaultLocation="/usr/sbin/apachectl";
		if [[ ! -f ${defaultLocation} ]] || [[ ! -x ${defaultLocation} ]]; then
			echo "NOT found in /usr/sbin/apachectl...";
		else
			# looks good, set the variable
			myApacheCTL="/usr/sbin/apachectl";
			local ctlFileFound=1;
                        echo "[SUCCESS]";
                fi
	
		local defaultLocation="/usr/sbin/apache2ctl";
                if [[ ! -f ${defaultLocation} ]] || [[ ! -x ${defaultLocation} ]]; then
                        echo "NOT found in /usr/sbin/apache2ctl...";
                else
                        # looks good, set the variable
                        myApacheCTL="/usr/sbin/apache2ctl";
			local ctlFileFound=1;
                        echo "[SUCCESS]";
                fi
			
		if [[ $ctlFileFound -eq 0 ]]; then
                        echo "[FAIL]";
                        echo "Error: Apache control file not provided and not in default location. Unable to continue.";
                        echo "Use the -c switch to specify the location of the 'apachectl' file manually.";
                        echo "Exiting...";
                        exit 1;
		fi

	elif [[ $myLinuxVersion == *Darwin*  ]]; then
		echo "[SUCCESS]";
		echo -n "Checking default location of ApacheCTL...";

		local ctlFileFound=0;

		# test the default location
		local defaultLocation="/usr/sbin/apachectl";
		if [[ ! -f ${defaultLocation} ]] || [[ ! -x ${defaultLocation} ]]; then
			echo "NOT found in /usr/sbin/apachectl...";
		else
			# looks good, set the variable
			myApacheCTL="/usr/sbin/apachectl";
			local ctlFileFound=1;
                        echo "[SUCCESS]";
        fi
	
		local defaultLocation="/usr/sbin/apache2ctl";
                if [[ ! -f ${defaultLocation} ]] || [[ ! -x ${defaultLocation} ]]; then
                        echo "NOT found in /usr/sbin/apache2ctl...";
                else
                        # looks good, set the variable
                        myApacheCTL="/usr/sbin/apache2ctl";
			local ctlFileFound=1;
                        echo "[SUCCESS]";
                fi
			
		if [[ $ctlFileFound -eq 0 ]]; then
                        echo "[FAIL]";
                        echo "Error: Apache control file not provided and not in default location. Unable to continue.";
                        echo "Use the -c switch to specify the location of the 'apachectl' file manually.";
                        echo "Exiting...";
                        exit 1;
		fi

	else
		echo "[FAIL]";
                echo "Error: Apache control file not provided and no default exists for this OS.";
                echo "Use the -c switch to specify the location of the 'apachectl' file manually.";
                echo "Exiting...";
                exit 1;
	fi
}

function autodetectCFMLHome {
        # this function will be called if the $myCFMLHome variable is blank
        # and can be expanded upon as different OS's are tried and as OS's evolve.
	
	echo -n "CFMLHome undefined, trying to autodetect...";
	
	# set the old home to blank so we can test it.
	myCFMLHome="";
	
	local defaultLocation="/opt/lucee";
	
	# test for a lucee install
        if [[ -d $defaultLocation ]] && [[ -d "$defaultLocation/tomcat/" ]]; then
		echo "[SUCCESS]";
		myCFMLHome=$defaultLocation;
        fi
	
	if [[ -z $myCFMLHome ]]; then
		local defaultLocation="/opt/openbd";
		
		# test for an openbd install
	        if [[ -d $defaultLocation ]] && [[ -d "$defaultLocation/tomcat/" ]]; then
	                echo "[SUCCESS]";
	                myCFMLHome=$defaultLocation;
	        fi
	fi
	
	if [[ -z $myCFMLHome ]]; then
		# if we're still empty, script can't find it.
		echo "[FAIL]";
		echo "Error: No CFML Server directory provided and can't autodetect.";
		echo "You can manually set the CFML server directory using the -l switch.";
                echo "Exiting...";
                exit 1;
	fi
}

function audodetectApacheConf {
	# this function will be called if the $myApacheConf variable is blank
        # and can be expanded upon as different OS's are tried and as OS's evolve.

        echo "ApacheConf undefined, trying to autodetect...";

	myApacheConf="";

        # use the getLinuxVersion function to try and see if we know what we're being run on
        # GetLinuxVersion will return myLinuxVersion
        getLinuxVersion;

        if [[ $myLinuxVersion == *RedHat*  ]]; then
                echo "Detected RedHat-based build.";
                echo -n "Checking default location for Apache config...";
		
		# test the default location
		local defaultLocation="/etc/httpd/conf/httpd.conf";
		
	        if [[ ! -f $defaultLocation ]]; then
                        echo "[FAIL]";
                        echo "Error: Apache config file not provided and not in default location. Unable to continue.";
                        echo "Use the -f switch to specify the location of the Apache config file manually.";
                        echo "Exiting...";
                        exit 1;
                else
                        # looks good, set the variable
                        myApacheConf=$defaultLocation;
                        echo "[SUCCESS]";
			fi

		elif [[ $myLinuxVersion == *Debian*  ]]; then
                echo "Detected Debian-based build.";
                echo -n "Checking default location of Apache config...";

                # test the default location
                local defaultLocation="/etc/apache2/apache2.conf";
                if [[ ! -f ${defaultLocation} ]]; then
                        echo "[FAIL]";
                        echo "Error: Apache config file not provided and not in default location. Unable to continue.";
                        echo "Use the -f switch to specify the location of the 'apachectl' file manually.";
                        echo "Exiting...";
                        exit 1;
                else
                        # looks good, set the variable
                        myApacheConf=$defaultLocation;
                        echo "[SUCCESS]";
                fi
		elif [[ $myLinuxVersion == *Darwin*  ]]; then
                echo "Detected Darwin-based build.";
                echo -n "Checking default location of Apache config...";

                # test the default location
                local defaultLocation="/etc/apache2/httpd.conf";
                if [[ ! -f ${defaultLocation} ]]; then
                        echo "[FAIL]";
                        echo "Error: Apache config file not provided and not in default location. Unable to continue.";
                        echo "Use the -f switch to specify the location of the 'apachectl' file manually.";
                        echo "Exiting...";
                        exit 1;
                else
                        # looks good, set the variable
                        myApacheConf=$defaultLocation;
                        echo "[SUCCESS]";
                fi
        fi


        if [[ -z $myApacheConf ]]; then
                # if we're still empty, script can't find it.
                echo "Error: No Apache config file provided and can't autodetect.";
                echo "You can manually set the Apache config file using the -f switch.";
                echo "Exiting...";
                exit 1;
        fi
}

function checkModPerl {
        # configure found variable
        modPerlFound=0;

        # check the variations we know of. Additional functional variations can be
        # added to this loop as we find them.

        # look for perl_module in stdout (ubuntu)
        echo -n "Checking for 'perl_module' in stdout...";
        searchFoundPerl=`$myApacheCTL -M | grep -c perl_module`;
        if [[ "$searchFoundPerl" -eq "0" ]]; then
                echo "[NOT FOUND]";
        else
                echo "[FOUND]";
                modPerlFound=1;
        fi

        # look for perl_module in stderr (ubuntu)
        if [[ "$modPerlFound" -eq "0" ]]; then
                echo -n "Checking for 'perl_module' in stderr...";
                searchFoundPerl=`$myApacheCTL -M 2>&1 | grep -c perl_module`;
                if [[ "$searchFoundPerl" -eq "0" ]]; then
                        echo "[NOT FOUND]";
                else
                        echo "[FOUND]";
                        modPerlFound=1;
                fi
        fi

	if [[ "$modPerlFound" -eq "0" ]]; then
		if [[ $myMode = "install" ]] && [[ -z "$installModPerlHit" ]]; then
			installModPerl;
		else
                	echo "";
	                echo "FATAL: mod_perl not found in Apache. Is mod_perl installed?";
        	        exit 1;
		fi
        fi
}

function checkModCFMLAlreadyInstalled {
	echo -n "Checking for pre-existing mod_cfml install...";
	
	myModCFMLFound=`cat ${myApacheConf} | grep -c mod_cfml`;

	if [[ "$myModCFMLFound" -gt "0" ]]; then
		echo "[FOUND]";
                echo "FATAL: mod_cfml looks like it is already installed.";
		echo "Please remove all references to 'mod_cfml' in your Apache config and try again.";
                exit 0;
	fi
}

function installModPerl {
	# this function attempts to install mod_perl via a distro's repository
	echo -n "Attempting to install mod_perl...";

        # record that we've gone through the install process so we don't hit a loop
        installModPerlHit=1;

        # use the getLinuxVersion function to try and see if we know what we're being run on
        # GetLinuxVersion will return myLinuxVersion
        getLinuxVersion;

        # default to redhat version so we can search for YUM by default. This is specifically to support
        # Amazon Linux, which is RHEL-based and uses YUM even though it doesn't bring up a name in the
        # audo-detect function.
        if [[ ! $myLinuxVersion == *RedHat* ]] && [[ ! $myLinuxVersion == *Debian* ]] && [[ ! $myLinuxVersion == *Darwin* ]]; then
                echo "$myLinuxVersion Cannot detect Linux version. Defaulting to RedHat/YUM...";
                myLinuxVersion="Amazon RedHat Linux";
        fi

        if [[ $myLinuxVersion == *RedHat*  ]]; then
                echo "Detected RedHat-based build.";

		# make sure we can use YUM
		detectYumExists;
		
		# try to install mod_perl with YUM
		yum -y install mod_perl;
		
		# see if the YUM command worked or not
		if [[ "$?" -ne "0" ]]; then
			# if the command response is not 0, the command failed
			echo "FATAL: 'yum' install failed. You will need to install mod_perl manually.";
			echo "Exiting...";
			exit 1;
		else
			# if the command worked, restart apache and test mod_perl again
			$myApacheCTL restart;
			if [[ "$?" -ne "0" ]]; then
				# if the command response is not 0, the command failed
                        	echo "FATAL: Can't restart Apache using supplied 'apachectl' command.";
                        	echo "Exiting...";
				exit 1;
			fi
		fi

        elif [[ $myLinuxVersion == *Debian*  ]]; then
                echo "Detected Debian-based build.";

                # make sure we can use APT-GET
                detectAPTExists;

                # try to install mod_perl with APT-GET
		apt-get -y install libapache2-mod-perl2;
		a2enmod perl;

                # see if the perl is now enabled
                if [[ "$?" -ne "0" ]]; then
                        # if the command response is not 0, the command failed
                        echo "FATAL: 'apt-get' mod_perl install failed. You will need to install mod_perl manually.";
                        echo "Exiting...";
			exit 1;
                else
                        # if the command worked, restart apache and test mod_perl again
                        $myApacheCTL restart;
                        if [[ "$?" -ne "0" ]]; then
                                # if the command response is not 0, the command failed
                                echo "FATAL: Can't restart Apache using supplied 'apachectl' command.";
                                echo "Exiting...";
				exit 1;
                        fi
                fi
        elif [[ $myLinuxVersion == *Darwin*  ]]; then
                echo "Detected Darwin-based build.";
				echo -e "LoadModule perl_module libexec/apache2/mod_perl.so \n#modcfml_added_the_above_perl_module" >> $myApacheConf

                        # if the command worked, restart apache and test mod_perl again
                        $myApacheCTL restart;
                        if [[ "$?" -ne "0" ]]; then
                                # if the command response is not 0, the command failed
                                echo "FATAL: Can't restart Apache using supplied 'apachectl' command.";
                                echo "Exiting...";
				exit 1;
                        fi
        fi
        echo "[DONE]";
        checkModPerl;
}

function detectYumExists {
        echo -n "Checking for 'yum' executable in the PATH...";
        hash yum &> /dev/null
        if [[ $? -eq 1 ]]; then
                echo "";
                echo "FATAL: 'yum' executable doesn't exist in PATH. Automatic installation impossible.";
		echo "Exiting...";
                exit 1;
        fi
        echo "[FOUND]";
}

function detectAPTExists {
	echo -n "Checking for 'apt-get' executable in the PATH...";
        hash apt-get &> /dev/null
        if [[ $? -eq 1 ]]; then
                echo "";
                echo "FATAL: 'apt-get' executable doesn't exist in PATH. Automatic installation impossible.";
                echo "Exiting...";
                exit 1;
        fi
        echo "[FOUND]";
}

function installModCFML {
	# install Apache config
	echo -n "Installing mod_cfml into Apache config...";
	echo "" >> $myApacheConf;
	echo "PerlRequire ${myCFMLHome}/tomcat_connectors/mod_cfml/mod_cfml.pm" >> $myApacheConf;
	echo "PerlHeaderParserHandler mod_cfml" >> $myApacheConf;
	echo "PerlSetVar LogHeaders false" >> $myApacheConf;
	echo "PerlSetVar LogHandlers false" >> $myApacheConf;
	echo "PerlSetVar CFMLHandlers \".cfm .cfc .cfml\"" >> $myApacheConf;
	echo "" >> $myApacheConf;
	echo "[SUCCESS]";
}

#####################
# Run function list #
#####################

# start by verifying input
verifyInput;

# functions will depend on the mode
if [[ $myMode = "install" ]]; then
	# install mode functions
        # checkPerlExists;
        checkModPerl;
	checkModCFMLAlreadyInstalled;
	installModCFML;
	echo "Installation Complete";
elif [[ $myMode = "test" ]]; then
	# test mode functions
	# checkPerlExists;
	checkModPerl;
	echo "Testing completed.";
fi
