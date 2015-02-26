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
 <cfcontent type="text/csv; charset=utf-8" reset="true"> 
<cfif url.headers>
	<cfoutput>"ColA","ColB","ColC"#chr(10)#</cfoutput>
</cfif>
<cfparam name="url.rows" default=0>
<cfloop index="ndx" from="1" to="#url.rows#">
	<cfoutput>"Row#ndx#",#ndx#,#ndx*2+1##chr(10)#</cfoutput>
</cfloop>