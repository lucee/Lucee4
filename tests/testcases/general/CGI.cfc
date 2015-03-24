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

	public void function testNonExisting(){
		http method="get" result="local.result" url="#createURL("CGI/index.cfm?name=doesNotExists")#" addtoken="false";
		assertEquals("",result.filecontent.trim());
	}

	public void function testHeader(){
		http method="get" result="local.result" url="#createURL("CGI/index.cfm?name=exist")#" addtoken="false" {
			httpparam type="header" name="exist" value="valueExists";
		}
		
		assertEquals("valueExists",result.filecontent.trim());
	}

	public void function testHeaderWithHyphen(){
		http method="get" result="local.result" url="#createURL("CGI/index.cfm?name=existing_name")#" addtoken="false" {
			httpparam type="header" name="existing-name" value="existing-value";
		}
		
		assertEquals("existing-value",result.filecontent.trim());
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>