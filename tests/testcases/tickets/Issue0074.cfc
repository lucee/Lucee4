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
	pageencoding "UTF-8";

	variables.webAdminPassword="server";

	public function setUp(){
		http method="get" result="local.result" url="#createURL("Issue0074/init.cfm?pw=#webAdminPassword#")#" addtoken="false";

	}

	public void function testJSON() localmode="true" {
		//http method="get" result="local.result" url="#createURL("Issue0074/xml.cfm")#" addtoken="false";
		//echo(result.filecontent);
		id = randRange(1,1000);
		http method="get" result="local.result" url="#createRestURL("issue0074/person/#id#")#" addtoken="false" charset="UTF-8" {
			httpparam type="header" name="accept" value="application/json";
		}
		
		// parse json and get name
		name=deserializeJson(result.filecontent).data[1][3];
		assertEquals("Sébastien",name);

		// extract the name directly from xml string (just to make sure)
		index=find('bastien',result.filecontent);
		assertEquals(chr(233),mid(result.filecontent,index-1,1)); // im chosing this form of character defintion to exclude any bug in the parsing process that could benefit a wrong result (2 times wrong==right)
	}

	public void function testXML() localmode="true" {
		//http method="get" result="local.result" url="#createURL("Issue0074/xml.cfm")#" addtoken="false";
		//echo(result.filecontent);
		id = randRange(1,1000);
		http method="get" result="local.result" url="#createRestURL("issue0074/person/#id#")#" addtoken="false" charset="UTF-8" {
			httpparam type="header" name="accept" value="text/xml";
		}
		
		// parse xml and get name
		name=xmlParse(result.filecontent).query.rows.row.column[3].xmlText;
		assertEquals("Sébastien",name);

		// extract the name directly from xml string (just to make sure)
		index=find('bastien',result.filecontent);
		assertEquals(chr(233),mid(result.filecontent,index-1,1)); // im chosing this form of character defintion to exclude any bug in the parsing process that could benefit a wrong result (2 times wrong==right)
		
		
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	private string function createRestURL(string path){
		return "http://#cgi.HTTP_HOST#/rest/#path#";
	}
	
} 
</cfscript>