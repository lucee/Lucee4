#!/bin/sh
###############################################################################
# Purpose:      to remove the default mod_proxy config from Apache conf file
# Author:       Jordan Michaels (jordan@viviotech.net)
# License:      LGPL 3.0
#               http://www.opensource.org/licenses/lgpl-3.0.html
#
# Usage:        remove_mod_proxy.sh /path/to/apache.conf
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
	echo "Usage: ./remove_connector.sh /path/to/apache.conf";
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
# grep the conf file to see if mod_jk is present already
JKLineCount=`cat $myApacheConfigFile | grep -c mod_proxy.c`;
if [ $JKLineCount -eq 0 ] ; then
        echo "It doesn't look like mod_proxy is configured...";
	echo "Exiting...";
	exit;
fi

# remove the <IfModule !mod_jk.c> segment...
sed -i '' '/<IfModule mod_proxy.c>/,/<\/IfModule>/d' $myApacheConfigFile
	
echo "";
echo "Mod_Proxy entries removed...";
echo "Apache config updated sucessfully.";
echo "";
