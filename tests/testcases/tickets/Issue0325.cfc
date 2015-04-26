<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
	
	variables.curr=getDirectoryFromPath(getCurrentTemplatePath());

	public void function testCreateObjectJava(){
		local.classFile=curr&"Issue0325/issue325/Test325.class";
		assertTrue(fileExists(classFile)); // just to make sure the path is valid
		createObject("java","issue325.Test325",classFile);
	}
	public void function testCreateObjectJavaNoPackage(){
		local.classFile=curr&"Issue0325/issue325/Test325.class";
		assertTrue(fileExists(classFile)); // just to make sure the path is valid
		createObject("java","Test325",classFile);
	}

} 
</cfscript>