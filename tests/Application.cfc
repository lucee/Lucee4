/**
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
 **/
component {
	this.name = hash( getCurrentTemplatePath() );
    


	include "properties.cfm";

	// check properties
	if(isNull(request.webAdminPassword) || request.webAdminPassword.isEmpty())
		throw '"request.webAdminPassword" is not set in template "properties.cfm"';

	if(isNull(request.mysql) || request.mysql.isEmpty())
		throw '"request.mysql" is not set in template "properties.cfm"';
	

	// addional path

	if(!isNull(request.external)) {
		mapp={};
		loop struct="#request.external#" index="label" item="path" {
			virtual="/"&label.replace(' ','_');
			admin 
				action="updateMapping"
				type="web"
				password="#request.webAdminPassword#"
				archive=""
				primary="physical"
				trusted="false"
				virtual="#virtual#"
				physical="#path#";

			if(fileExists(path&"/properties.cfm")) {
				//mapp[virtual]=path;
				//application action="update" mappings="#{''&virtual:path}#";
				include virtual&"/properties.cfm";
			}
		}
		this.mappings=mapp;
	}





	// make sure testbox exists 
	// TODO cache this test for a minute
	try{
		getComponentMetaData("testbox.system.TestBox");
	}
	catch(e){ 
		// only add mapping when necessary
		this.componentpaths = [{archive:getDirectoryFromPath(getCurrentTemplatePath())&"testbox.lar"}]; // "{lucee-server}/context/testbox.ra"
	}
	

}