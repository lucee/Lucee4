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
	
	variables.xmlFile=getDirectoryFromPath(getCurrentTemplatePath())&"LDEV0494.xml";

	public void function test(){
		try{
			fileWrite(variables.xmlFile,'<html><body><br/><hr/><susi/></body></html>');	
			local.xml=xmlParse(xmlFile);
			//dump(xml);
			echo(replace(replace(toString(xml),'<','{','all'),'>','}','all'));
			assertEquals("<html><body><br></br><hr></hr><susi></susi></body></html>",toString(xml));
		}
		finally{
			fileDelete(xmlFile);
		}
	}
} 
</cfscript>