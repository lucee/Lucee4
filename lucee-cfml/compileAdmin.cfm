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
 ---><cfsetting showdebugoutput="No" enablecfoutputonly="Yes">

<cftry>

    <cfset virtualPath = "/lucee-context-compiled">

	<cfadmin action="updateMapping" type="web" password="#url.password#"
		virtual="#virtualPath#"
		physical="#url.admin_source#"
		primary="physical"
		trusted="false"
		archive="">

	<cfadmin action="createArchive" type="web" password="#url.password#"
		virtual="#virtualPath#"
		file="#url.admin_source#/lucee-context.ra"
		addCFMLFiles="false"
		addNonCFMLFiles="false"
		append="false">

	<cfcatch type="Any">
		<cfoutput>Mapping not created. Error occured. (#cfcatch.message#)</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>lucee Admin compiled...</cfoutput>
