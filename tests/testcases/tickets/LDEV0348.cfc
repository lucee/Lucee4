<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	BufferedReader=createObject('java','java.io.BufferedReader');
	IOException=createObject('java','java.io.IOException');
	InputStream=createObject('java','java.io.InputStream');
	OutputStream=createObject('java','java.io.OutputStream');
	Socket=createObject('java','java.net.Socket');
	InputStreamReader=createObject('java','java.io.InputStreamReader');
	NL="
";

	public void function test(){
		var rsp=call("GET",cgi.server_name,cgi.server_port,
			getDirectoryFromPath(contractPath(getCurrenttemplatepath()))&"LDEV0348/index.cfm");
		assertTrue(findNoCase("200 OK",rsp));
	}



	private function call(method,host,port, scriptName) {
		var socket = Socket.init(host,port);
		var out = socket.getOutputStream(); 
		var in = socket.getInputStream(); 
		var i=0;
		var br="";
		var sMessage = ucase(method)&" "&scriptName&" HTTP/1.1"&NL
		&"Host:"&host&NL
		&"Cookie:qqq<!--##include file -->=qqq "&NL

		&NL; 
	
		var aby = sMessage.getBytes(); 
		var line="";
		var rsp="";

		for (i=1;i lte arrayLen(aby);i=i+1) {
			out.write(aby[i]); 
		}
		out.flush(); 
		
		br = BufferedReader.init(InputStreamReader.init(in));
	    while(true) {
			line=br.readLine();
	    	rsp&=line&"-";
	    	if(line EQ "") break;
	    }

		in.close();
		out.close();

		return rsp;
	}
	
	
} 
</cfscript>