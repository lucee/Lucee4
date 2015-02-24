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

	variables.ticketNumber="0188";
	//public function setUp(){}

	public void function testWithHeadersWithData(){
		http name="local.qry" method="get" result="local.result" 
			url="#createURL("Issue#ticketNumber#/index.cfm?headers=yes&rows=2")#" addtoken="false";
		assertEquals("COLA,COLB,COLC",qry.columnlist);
		assertEquals(2,qry.recordcount);
		assertEquals("Row1",qry.cola[1]);
		assertEquals(2,qry.colb[2]);
	}

	public void function testWithHeadersNoData(){
		http name="local.qry" method="get" result="local.result" 
			url="#createURL("Issue#ticketNumber#/index.cfm?headers=yes&rows=0")#" addtoken="false";
		assertEquals("COLA,COLB,COLC",qry.columnlist);
		assertEquals(0,qry.recordcount);
	}

	public void function testNoHeadersWithData(){
		http name="local.qry" method="get" result="local.result" firstrowasheaders="no" columns="colA, colB, colC"
			url="#createURL("Issue#ticketNumber#/index.cfm?headers=no&rows=2")#" addtoken="false";
		assertEquals("COLA,COLB,COLC",qry.columnlist);
		assertEquals(2,qry.recordcount);
		assertEquals("Row1",qry.cola[1]);
		assertEquals(2,qry.colb[2]);
	}

	public void function testNoHeadersNoData(){
		http name="local.qry" method="get" result="local.result" firstrowasheaders="no" columns="colA, colB, colC"
			url="#createURL("Issue#ticketNumber#/index.cfm?headers=no&rows=0")#" addtoken="false";
		//echo(result.fileContent);
		assertEquals("COLA,COLB,COLC",qry.columnlist);
		assertEquals(0,qry.recordcount);
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>