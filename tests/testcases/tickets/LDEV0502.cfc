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

	function run(){

		describe( 'java.nio.file.attribute.BasicFileAttributes' , function() {

			beforeEach( function() {

				proxy = CreateObject(
					'java',
					'java.nio.file.attribute.BasicFileAttributes'
				);

			});

			it( 'is a java.lang.Class that implements the interface specified' , function() {

				expect( proxy ).toBeInstanceOf( 
					'java.nio.file.attribute.BasicFileAttributes'
				);

				c = proxy.getClass();

				expect( c ).toBeInstanceOf( 
					'java.lang.Class'
				);
				expect( 
					proxy.getCLass().getName() 
				).toBe( 
					'java.nio.file.attribute.BasicFileAttributes' 
				);

			});


		});

	}

}
</cfscript>