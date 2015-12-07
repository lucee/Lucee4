<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

	<cffunction name="testTagBased">
		<cfset local.res=tagBased(new LDEV0461(),[new LDEV0461()])>
	</cffunction>

	<cffunction name="testScriptBased">
		<cfset local.res=scriptBased(new LDEV0461(),[new LDEV0461()])>
	</cffunction>



	<cffunction access="private" name="tagBased" returntype="LDEV0461[]">
		<cfargument name="arg1" type="LDEV0461">
		<cfargument name="arg2" type="LDEV0461[]">
		<cfreturn arg2>
	</cffunction>
	<cfscript>
	private function scriptBased(LDEV0461 arg1, LDEV0461[] arg2){
		return arg2;
	}
	</cfscript>

</cfcomponent>