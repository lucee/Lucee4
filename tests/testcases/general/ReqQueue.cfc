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

	//public function setUp(){}

	public void function test(){
		var config=getPageContext().getConfig();
		var factory=config.getFactory();
		max=config.getQueueMax();
		local.names=[];
		local.https={};
		request.listen=true;
		request.active=0;
		
		/*
		* this thread check the size of the queue and how many active request are present and logs the highest number.
		*/
		thread name="tlistener" {
			count=0;
			config=getPageContext().getConfig();
			factory=config.getFactory();
			active=0;
			
			while(request.listen) {
				count++;
				var pcs=factory.getActivePageContexts();
				var it=pcs.values().iterator();
				a=0;
				loop collection="#it#" item="local.pc" {
					if(isNull(pc.getParentPageContext()))
						a++;
				}
				if(a>active) {
					request.active=a-config.getThreadQueue().size();
					active=a;
				}
				sleep(1);
			}
		}

		loop from="1" to="#max*2#" index="local.i" {
			arrayAppend(names,"t#i#");
			thread name="t#i#" n="t#i#" https="#local.https#" {
				http method="get" result="local.result" url="#createURL("ReqQueue/index.cfm")#" addtoken="false";
				attributes.https[attributes.n]=result;
			}
		}
		thread action="join" name="#names.toList()#";
		request.listen=false;
		thread action="join" name="tlistener";

		assertTrue(request.active<=max);
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 
</cfscript>