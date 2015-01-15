@echo off
rem ---------------------------------------------------------------------------
rem autostart.bat
rem written by: Jordan Michaels (jordan@viviotech.net)
rem date: August, 2011
rem purpose: to programmatically set the Lucee service to start automatically
rem ---------------------------------------------------------------------------
rem THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
rem IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
rem FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
rem AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
rem LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
rem FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
rem DEALINGS IN THE SOFTWARE.
rem ---------------------------------------------------------------------------

rem save the current directory so we can return here after we're done
set CURRENT_DIR=%cd%
rem CD to our proper home directory
rem (this script has to be run from the %CATALINA_HOME%\bin\ directory)
cd ..
set CATALINA_HOME=%cd%

rem see if an executable name was stated in the command-line params
if "x%1x" == "xx" (goto setDefault) ELSE (goto checkExist)

:setDefault
set SERVICE_FILE=Tomcat7.exe
goto setService

:checkExist
set SERVICE_FILE=%1
if not exist %SERVICE_FILE% goto fileError

:fileError
echo That service file doesn't exist.
goto end

:setService
rem set the tomcat/lucee service to start automatically
set EXECUTABLE=%CATALINA_HOME%\bin\%SERVICE_FILE%
set SERVICE_NAME=Lucee
%EXECUTABLE% //US//%SERVICE_NAME% --Startup=auto

rem let the user know we're good to go
echo the %SERVICE_NAME% Service will now start automatically after a reboot.	
goto end

:end
cd "%CURRENT_DIR%"
