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
			END;
			$BODY$
			  LANGUAGE plpgsql VOLATILE
			  COST 100;
			--ALTER FUNCTION f_is_bigint(bigint)
			--  OWNER TO postgres;
		</cfquery>


		<cfstoredproc procedure="f_is_bigint" datasource="postgre" debug="yes">
		<cfprocparam type="In" cfsqltype="CF_SQL_BIGINT" value="2147483649" null="no">
		<!--- result set --->
		<cfprocresult name="data3">
		</cfstoredproc>


		<cfset assertEquals(true,isQuery(data3))>
		<cfset assertEquals(1,data3.recordcount)>
		<cfset assertEquals("out_is_bigint,out_value",data3.columnlist)>
		<cfset assertEquals(1,data3.out_is_bigint)>
		<cfset assertEquals(2147483649,data3.out_value)>
	</cffunction>
</cfcomponent>