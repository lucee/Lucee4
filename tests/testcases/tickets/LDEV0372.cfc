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

	public void function testAppCFCWritable(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfc/index.cfm?readonly=false")#" addtoken="false";
		assertEquals("Susanne-test-80",result.filecontent.trim());
	}
	public void function testAppCFCReadonly(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfc/index.cfm?readonly=true")#" addtoken="false";
		assertEquals("can't set key [susi] to struct, struct is readonly",result.filecontent.trim());
	}

	public void function testAppCFCDefault(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfc/index.cfm")#" addtoken="false";
		var readOnly=getPageContext().getConfig().getCGIScopeReadOnly();

		assertEquals(
			readOnly?
			"can't set key [susi] to struct, struct is readonly":
			"Susanne-test-80",
			result.filecontent.trim());
	}


	public void function testAppCFMDefault(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfm/index.cfm")#" addtoken="false";
		var readOnly=getPageContext().getConfig().getCGIScopeReadOnly();

		assertEquals(
			readOnly?
			"can't set key [susi] to struct, struct is readonly":
			"Susanne-test-80",
			result.filecontent.trim());
	}

	public void function testAppCFMWritable(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfm/index.cfm?readonly=false")#" addtoken="false";
		assertEquals("Susanne-test-80",result.filecontent.trim());
	}
	public void function testAppCFMReadonly(){
		http method="get" result="local.result" url="#createURL("LDEV0372/appcfm/index.cfm?readonly=true")#" addtoken="false";
		assertEquals("can't set key [susi] to struct, struct is readonly",result.filecontent.trim());
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return local.baseURL&""&arguments.calledName;
	}
	
} 
</cfscript>