#!/bin/bash
#
# ----------------------------------------------------------------------------------
# Purpose:	Removes a user
#
# Usage: 	remove_user.sh [username] [install dir]
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
        echo "Usage: ./remove_user.sh [username] /path/to/installdir";
        exit 1;
elif [[ ! $1 =~ ^[a-z_][a-zA-Z0-9_-]+$ ]]; then  # make sure username is a valid format
        echo "Error: Invalid User Name";
	echo "";
	echo "Rules for User Names:";
	echo "1) User Names must start with a lower-case letter"
	echo "2) User Names must contain only alphanumeric characters, hyphens, or underscores.";
	echo "";
        echo "Usage: ./remove_user.sh [username] /path/to/installdir";
        exit 1;
else
        myUserName=$1;
fi

sudo dscl . delete /Users/${myUserName};
sudo dscl . delete /Groups/${myUserName};
echo "[DELETED] ${myUserName}";
