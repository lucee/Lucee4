<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	public void function testOnRequest(){
		http method="get" result="local.result" url="#createURL("Issue0310/notExisting.lucee")#" addtoken="false";
		assertEquals("onRequest",result.filecontent);

		http method="get" result="local.result" url="#createURL("Issue0310/test.lucee")#" addtoken="false";
		assertEquals("onRequest",result.filecontent);
	}
	public void function testOnCFCRequest(){
		http method="get" result="local.result" url="#createURL("Issue0310/CTest.lucee?method=whatever&returnFormat=json")#" addtoken="false";
		assertEquals('"onCFCRequest"',result.filecontent);
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>