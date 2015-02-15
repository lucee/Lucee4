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
 ---><cfsetting showdebugoutput="no">
 <cfscript>
	id = randRange(1,1000);
		dump("#createRestURL("issue0074/person/#id#")#");
		http method="get" result="result" url="#createRestURL("issue0074/person/#id#")#" addtoken="false" charset="UTF-8" {
			httpparam type="header" name="accept" value="text/xml";
		}
		dump(result);


	string function createRestURL(string path){
		return "http://#cgi.HTTP_HOST#/rest/#path#";
	}

</cfscript>qq