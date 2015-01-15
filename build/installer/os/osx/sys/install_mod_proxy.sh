#!/bin/bash
###############################################################################
# Purpose: 	installs mod_proxy (if needed) and configure Apache for CFML
# Author: 	Jordan Michaels (jordan@viviotech.net)
# License:	LGPL 3.0
# 		http://www.opensource.org/licenses/lgpl-3.0.html
#
# Usage:	install_mod_proxy.sh
#		-m [install|test]
#			install: will perform tests and install if all tests
#				pass.
#			test: will perform tests to ensure system meets
#				requirements.
#		-t [html-port-number]
#			Use this port if you're proxying to a secondary web
#			server, such as the built-in web server in tomcat.
#		-f /path/to/apache.conf
#			Full system path to Apache config file.
#			IE: /etc/apache/apache2.conf
#		-c /path/to/apachectl
#			Full system path to Apache Control Script.
#			IE: /usr/sbin/apachectl
#		-h
#			Displays usage info
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
   -t	[8888]			: The HTTP Port Number for Tomcat. Will default
				  to 8888.
   -f	/path/to/apache.conf	: Full system path to Apache config file.
				  IE: /etc/apache2/apache2.conf (deb/ubuntu)
				  IE: /etc/httpd/conf/httpd.conf (rhel/centos)
   -c	/path/to/apachectl	: Full system path to Apache Control Script.
				  IE: /usr/sbin/apachectl
   -h				: Displays this usage screen

EOF
}

# declare initial variables (so we can check them later)
myMode=
myHTTPPort=
myApacheConf=
myApacheCTL=
installModPerlHit=

# parse command-line params
while getopts “hm:l:p:t:f:c:” OPTION
do
     case $OPTION in
	 h)
	     usage
	     exit 1
	     ;;
         m)
             myMode=$OPTARG
             ;;
         t)
             myHTTPPort=$OPTARG
             ;;
         f)
             myApacheConf=$OPTARG
             ;;
         c)
             myApacheCTL=$OPTARG
             ;;
         ?)
             usage
	     exit 1
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
	        if [[ -z $myHTTPPort ]]; then
			# if no AJP or HTTP port, default to AJP 8009
			myHTTPPort=8888;
               	fi
	fi # close install mode checks
fi # close mode check

# verify myApacheCTL
if [[ -z $myApacheCTL ]] || [[ ! -x $myApacheCTL ]]; then
        # apachectl is needed for testing and install, try to autodetect
        autodetectApacheCTL;
fi

if [[ -z $myApacheConf ]] || [[ ! -f $myApacheConf ]]; then
	audodetectApacheConf;
fi
}

###################
# begin functions #
###################

function randomString {
	# if a param was passed, it's the length of the string we want
	if [[ -n $1 ]] && [[ "$1" -lt 20 ]]; then
		local myStrLength=$1;
	else
		# otherwise set to default
		local myStrLength=8;
	fi

	local mySeedNumber=$$`date +%N`; # seed will be the pid + nanoseconds
	local myRandomString=$( echo $mySeedNumber | md5sum | md5sum );
	# create our actual random string
	myRandomResult="${myRandomString:2:myStrLength}"
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

function checkModProxy {
	# configure found variable
	modProxyFound=0;
	
	# check the variations we know of. Additional functional variations can be
	# added to this loop as we find them.

	# look for proxy_html_module in stdout (ubuntu)
	echo -n "Checking for 'proxy_html_module' in stdout...";
	searchFoundProxy=`$myApacheCTL -M | grep -c proxy_html_module`;
	if [[ "$searchFoundProxy" -eq "0" ]]; then
		echo "[NOT FOUND]";
        else
                echo "[FOUND]";
		modProxyFound=1;
        fi
	
	# look for proxy_html_module in stderr (ubuntu)
	if [[ "$modProxyFound" -eq "0" ]]; then
	        echo -n "Checking for 'proxy_html_module' in stderr...";
	        searchFoundProxy=`$myApacheCTL -M 2>&1 | grep -c proxy_html_module`;
        	if [[ "$searchFoundProxy" -eq "0" ]]; then
                	echo "[NOT FOUND]";
	        else    
	                echo "[FOUND]";
			modProxyFound=1;
	        fi
	fi

        # look for proxy_http_module in stdout (centos)
	if [[ "$modProxyFound" -eq "0" ]]; then
	        echo -n "Checking for 'proxy_http_module' in stdout...";
	        searchFoundProxy=`$myApacheCTL -M | grep -c proxy_http_module`;
	        if [[ "$searchFoundProxy" -eq "0" ]]; then
	                echo "[NOT FOUND]";
	        else
	                echo "[FOUND]";
			modProxyFound=1;
	        fi
	fi

        # look for proxy_http_module in stderr (centos)
        if [[ "$modProxyFound" -eq "0" ]]; then
                echo -n "Checking for 'proxy_http_module' in stderr...";
                searchFoundProxy=`$myApacheCTL -M 2>&1 | grep -c proxy_http_module`;
                if [[ "$searchFoundProxy" -eq "0" ]]; then
                        echo "[NOT FOUND]";
                else
                        echo "[FOUND]";
			modProxyFound=1;
                fi
        fi
	
	if [[ "$modProxyFound" -eq "0" ]]; then
		if [[ $myMode = "install" ]] && [[ -z "$installModProxyHit" ]]; then
			installModProxy;
		else
                	echo "";
	                echo "FATAL: mod_proxy not found in Apache. Is mod_proxy installed?";
        	        exit 1;
		fi
        fi
}

function checkModPCFMLAlreadyInstalled {
	echo -n "Checking for pre-existing mod_proxy for CFML install...";
 	
	myModPCFMLFound=`grep -i "ProxyPassMatch" ${myApacheConf} | grep -c cfml`;

	# echo "myModPCFMLFound = ${myModPCFMLFound}";

	if [[ "$myModPCFMLFound" -gt "0" ]]; then
		echo "[FOUND]";
                echo "SOFT FAIL: mod_proxy for CFML looks like it is already installed.";
		echo "This script will leave what's already there alone.";
                exit 0;
	else
		echo "[NOT FOUND]";
	fi
}

function installModProxy {
	# this function attempts to install mod_perl via a distro's repository
	echo -n "Attempting to install mod_proxy...";

        # record that we've gone through the install process so we don't hit a loop
        installModProxyHit=1;

        # use the getLinuxVersion function to try and see if we know what we're being run on
        # GetLinuxVersion will return myLinuxVersion
        getLinuxVersion;

        # default to redhat version so we can search for YUM by default. This is specifically to support
        # Amazon Linux, which is RHEL-based and uses YUM even though it doesn't bring up a name in the
        # audo-detect function.
        if [[ ! $myLinuxVersion == *RedHat* ]] && [[ ! $myLinuxVersion == *Debian* ]] && [[ ! $myLinuxVersion == *Darwin* ]]; then
                echo "Cannot detect Linux version. Defaulting to RedHat/YUM...";
                myLinuxVersion="Amazon RedHat Linux";
        fi

        if [[ $myLinuxVersion == *RedHat*  ]]; then
                echo "Detected RedHat-based build.";
		echo "mod_proxy is installed by default with httpd in RHEL/CentOS builds;";
		echo "This script will not attempt to install mod_proxy on this system.";

        elif [[ $myLinuxVersion == *Debian*  ]]; then
                echo "Detected Debian-based build.";

                # make sure we can use APT-GET
                detectAPTExists;

                # try to install mod_proxy with APT-GET
		apt-get -y install libapache2-mod-proxy-html;
		a2enmod proxy;
		a2enmod proxy_http;

                # see if proxy is now enabled
                if [[ "$?" -ne "0" ]]; then
                        # if the command response is not 0, the command failed
                        echo "FATAL: 'apt-get' proxy install failed. You will need to install proxy manually.";
			echo "This script tried: 'apt-get -y install libapache2-mod-proxy-html' - which errored.";
                        echo "Exiting...";
			exit 1;
                else
                        # if the command worked, restart apache and test mod_proxy again
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
                # see if proxy is now enabled
                if [[ "$?" -ne "0" ]]; then
                        # if the command response is not 0, the command failed
                        echo "FATAL: 'apt-get' proxy install failed. You will need to install proxy manually.";
			echo "This script tried: 'apt-get -y install libapache2-mod-proxy-html' - which errored.";
                        echo "Exiting...";
			exit 1;
                else
                        # if the command worked, restart apache and test mod_proxy again
                        $myApacheCTL restart;
                        if [[ "$?" -ne "0" ]]; then
                                # if the command response is not 0, the command failed
                                echo "FATAL: Can't restart Apache using supplied 'apachectl' command.";
                                echo "Exiting...";
				exit 1;
                        fi
                fi
        fi
        echo "[DONE]";
        checkModProxy;
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

function installProxyCFML {
	if [[ ! -z $myHTTPPort ]]; then
		# connect using an HTTP proxy
		echo -n "Configuring mod_proxy_http for Apache...";
                echo "" >> $myApacheConf;
		echo "<IfModule mod_proxy.c>" >> $myApacheConf;
                echo "	<Proxy *>" >> $myApacheConf;
                echo "	Allow from 127.0.0.1" >> $myApacheConf;
                echo "	</Proxy>" >> $myApacheConf;
                echo "	ProxyPreserveHost On" >> $myApacheConf;
                echo "	ProxyPassMatch ^/(.+\.cf[cm])(/.*)?$ http://127.0.0.1:${myHTTPPort}/\$1\$2" >> $myApacheConf;
                echo "	ProxyPassMatch ^/(.+\.cfchart)(/.*)?$ http://127.0.0.1:${myHTTPPort}/\$1\$2" >> $myApacheConf;
                echo "	ProxyPassMatch ^/(.+\.cfres)(/.*)?$ http://127.0.0.1:${myHTTPPort}/\$1\$2" >> $myApacheConf;
                echo "	ProxyPassMatch ^/(.+\.cfml)(/.*)?$ http://127.0.0.1:${myHTTPPort}/\$1\$2" >> $myApacheConf;
                echo "	# optional mappings" >> $myApacheConf;
                echo "	#ProxyPassMatch ^/flex2gateway/(.*)$ http://127.0.0.1:${myHTTPPort}/flex2gateway/\$1" >> $myApacheConf;
                echo "	#ProxyPassMatch ^/messagebroker/(.*)$ http://127.0.0.1:${myHTTPPort}/messagebroker/\$1" >> $myApacheConf;
		echo "	#ProxyPassMatch ^/flashservices/gateway(.*)$ http://127.0.0.1:${myHTTPPort}/flashservices/gateway\$1" >> $myApacheConf;
		echo "	#ProxyPassMatch ^/openamf/gateway/(.*)$ http://127.0.0.1:${myHTTPPort}/openamf/gateway/\$1" >> $myApacheConf;
                echo "	#ProxyPassMatch ^/rest/(.*)$ http://127.0.0.1:${myHTTPPort}/rest/\$1" >> $myApacheConf;
                echo "	ProxyPassReverse / http://127.0.0.1:${myHTTPPort}/" >> $myApacheConf;
		echo "</IfModule>" >> $myApacheConf;
                echo "" >> $myApacheConf;
                echo "[SUCCESS]";
	else
		# bad things happened?
		echo "No port found for install?"
	fi
}

#####################
# Run function list #
#####################

# start by verifying input
verifyInput;

# functions will depend on the mode
if [[ $myMode = "install" ]]; then
	# install mode functions
        checkModProxy;
	checkModPCFMLAlreadyInstalled;
	installModProxy;
	installProxyCFML;
	echo "Proxy for CFML Installation Complete";
elif [[ $myMode = "test" ]]; then
	# test mode functions
	checkModProxy;
	checkModPCFMLAlreadyInstalled;
	echo "Testing complete.";
fi
