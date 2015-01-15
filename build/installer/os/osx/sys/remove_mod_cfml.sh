#!/bin/bash
###############################################################################
# Purpose:      to remove the default mod_cfml config from Apache conf file
# Author:       Jordan Michaels (jordan@viviotech.net)
# License:      LGPL 3.0
#               http://www.opensource.org/licenses/lgpl-3.0.html
#
# Usage:        remove_mod_cfml.sh /path/to/apache.conf
###############################################################################
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
###############################################################################

if [ ! $(id -u) = "0" ]; then
	echo "This script needs to be run as root.";
        echo "Exiting...";
        exit;
fi

# make sure the first parameter was specified and assign it a variable name
if [ -z $1 ]; then
	echo "No Apache config file specified."
	echo "Usage: ./remove_mod_cfml.sh /path/to/apache.conf";
	exit;
else
	myApacheConfigFile=$1;
fi


# Make sure the file exists
if [ ! -e $myApacheConfigFile ] ; then
        echo "The file you spefied doesn't exist.";
        echo "Exiting...";
	exit;
fi

echo "";
# grep the conf file to see if mod_cfml exists
LineCount=`cat $myApacheConfigFile | grep -c 'PerlHeaderParserHandler\ mod_cfml'`;
if [ $LineCount -eq 0 ] ; then
        echo "It doesn't look like mod_cfml is installed...";
	echo "Exiting...";
	exit;
fi

# remove the <IfModule !mod_jk.c> segment...
#sed -i '/<IfModule !mod_jk.c>/,/<\/IfModule>/d' $myApacheConfigFile
	
# remove the <IfModule mod_jk.c> segment...
#sed -i '/<IfModule mod_jk.c>/,/<\/IfModule>/d' $myApacheConfigFile
	
#echo "";
#echo "Mod_JK entries removed...";

sed -i '' '/^LoadModule perl_module/,/modcfml_added_the_above_perl_module/d' $myApacheConfigFile
sed -i '' '/^PerlRequire/,/mod_cfml.pm/d' $myApacheConfigFile
sed -i '' '/^PerlHeaderParserHandler\ mod_cfml/d' $myApacheConfigFile
sed -i '' '/^PerlSetVar\ LogHeaders/d' $myApacheConfigFile
sed -i '' '/^PerlSetVar\ LogHandlers/d' $myApacheConfigFile
sed -i '' '/^PerlSetVar\ CFMLHandlers/d' $myApacheConfigFile

echo "Mod_CFML entries removed...";

echo "Apache config updated sucessfully.";
echo "";
