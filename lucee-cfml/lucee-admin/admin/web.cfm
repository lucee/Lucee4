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
 --->					if(structKeyExists(stCld,'_action'))_action=stCld._action;
					else _action=stNavi.action & '.' & stCld.action;

					isfavorite = application.adminfunctions.isfavorite(_action);
					li = '<li' & (isfavorite ? ' class="favorite"':'') & '><a '&(isActive?'class="menu_active"':'class="menu_inactive"')&' href="' & request.self & '?action=' & _action & '"> ' & stCld.label & '</a></li>';
					if (isfavorite)
					{
						favoriteLis &= '<li class="favorite"><a href="#request.self#?action=#_action#">#stNavi.label# - #stCld.label#</a></li>';
					}
					subNav = subNav & li;
					//subNav = subNav & '<div class="navsub">'&arrow&'<a class="#sClass#" href="' & request.self & '?action=' & _action & '"> ' & stCld.label & '</a></div>';
				}
			}
		}
		strNav = strNav &'';
		hasChildren=hasChildren and len(subNav) GT 0;
		if(not hasChildren) {
			if(toBool(stNavi,"display"))strNav = strNav & '<li><a href="' & request.self & '?action=' & stNavi.action & '">' & stNavi.label & '</a></li>';
			//if(toBool(stNavi,"display"))strNav = strNav & '<div class="navtop"><a class="navtop" href="' & request.self & '?action=' & stNavi.action & '">' & stNavi.label & '</a></div>';
		}
		else {
			idName = toIDField(stNavi.label);
			isCollapsed = not hasActiveItem and application.adminfunctions.getdata('collapsed_' & idName) eq 1;
			strNav = strNav & '<li id="#idName#"#isCollapsed ? ' class="collapsed"':''#><a href="##">' & stNavi.label & '</a><ul#isCollapsed ? ' style="display:none"':''#>'&subNav& "</ul></li>";
			//strNav = strNav & '<div class="navtop">' & stNavi.label & '</div>'&subNav& "";
		}
		//strNav = strNav ;
	}
	strNav ='<ul id="menu">'& strNav&'</ul>' ;

/* moved to title in content area
   if (favoriteLis neq "")
   {
	   strNav = '<li id="favorites"><a href="##">Favorites</a><ul>' & favoriteLis & "</ul></li>" & strNav;
   }
   */

	function toBool(sct,key) {
		if(not StructKeyExists(arguments.sct,arguments.key)) return false;
		return arguments.sct[arguments.key];
	}
	function getRemoteClients() {
		if(not isDefined("form._securtyKeys")) return array();
		return form._securtyKeys;
	}
	function toIDField(value)
	{
		return "nav_" & rereplace(arguments.value, "[^0-9a-zA-Z]", "_", "all");
	}
	request.getRemoteClients=getRemoteClients;
</cfscript>

<cfif not StructKeyExists(session,"password"&request.adminType)>
		<cfadmin 
			action="hasPassword"
			type="#request.adminType#"
			returnVariable="hasPassword">
	<cfif hasPassword>
		<cfmodule template="admin_layout.cfm" width="480" title="Login" onload="doFocus()">
			<cfif login_error NEQ ""><span class="CheckError"><cfoutput>#login_error#</cfoutput></span><br></cfif>
			<cfinclude template="login.cfm">
		</cfmodule>
	<cfelse>
		<cfmodule template="admin_layout.cfm" width="480" title="New Password">
			<cfif login_error NEQ ""><span class="CheckError"><cfoutput>#login_error#</cfoutput></span><br></cfif>
			<cfinclude template="login.new.cfm">
		</cfmodule>
	</cfif>
<cfelse>
	<cfsavecontent variable="content">
		<cfif not FindOneOf("\/",current.action)>
			<cfinclude template="#current.action#.cfm">
		<cfelse>
			<cfset current.label="Error">
			invalid action definition
		</cfif>
	</cfsavecontent>
	
	<cfif request.disableFrame>
    	<cfoutput>#content#</cfoutput>
    <cfelse>
		<cfsavecontent variable="strNav">
			<script type="text/javascript">
				$(function() { 
					initMenu();
					__blockUI=function() {
						setTimeout(createWaitBlockUI(<cfoutput>"#JSStringFormat(stText.general.wait)#"</cfoutput>),1000);
					}
					$('.submit,.menu_inactive,.menu_active').click(__blockUI);
				}); 
			</script>
			<cfoutput>#strNav#</cfoutput>
		</cfsavecontent>
		
    	<cfmodule template="admin_layout.cfm" width="960" navigation="#strNav#" right="#context#" title="#current.label#" favorites="#favoriteLis#">
			<cfoutput>#content#</cfoutput>
        </cfmodule>
    </cfif>
</cfif>
<cfif current.action neq "overview">
	<cfcookie name="lucee_admin_lastpage" value="#current.action#" expires="NEVER">
</cfif>