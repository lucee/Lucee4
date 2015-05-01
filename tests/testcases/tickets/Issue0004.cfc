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

	//public function setUp(){}

	public void function testBody(){
		http method="get" result="local.result" url="#createURL("Issue0004/index.cfm?mode=body")#" addtoken="false";
		echo(result.filecontent)
		assertEquals('<html><head>Body</head><body></body></html>',trim(result.filecontent));
	}
	public void function testEnd(){
		http method="get" result="local.result" url="#createURL("Issue0004/index.cfm?mode=end")#" addtoken="false";
		echo(result.filecontent)
		assertEquals('<html><head>End</head><body></body></html>',trim(result.filecontent));
	}
	public void function testNone(){
		http method="get" result="local.result" url="#createURL("Issue0004/index.cfm?mode=none")#" addtoken="false";
		echo(result.filecontent)
		assertEquals('<html><head>none</head><body></body></html>',trim(result.filecontent));
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>