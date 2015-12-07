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
component extends="org.lucee.cfml.test.LuceeTestCase"   {
    CR = chr(13);
    LF = chr(10);
    CRLF = CR&LF;
    CRMarker = 'CR';
    LFMarker = 'LF';
    str='#CR#-#LF#-#CRLF#';
           
    private function visualiseLineBreaks(myString) {
        myString = replace(myString,CRLF,"CRLF","all");
        myString = replace(myString,CR,"CR","all");
        myString = replace(myString,LF,"LF","all");
        return myString;
    }
   
    public void function testUnformatted(){
        assertEquals("CR-LF-CRLF",visualiseLineBreaks(str));
    }

    public void function testHTMLEditFormat(){
        assertEquals("-LF-LF",visualiseLineBreaks(HTMLEditFormat(str)));
    }
    public void function testHTMLCodeFormat(){
        assertEquals("<pre>-LF-LF</pre>",visualiseLineBreaks(HTMLCodeFormat(str)));
    }
} 
</cfscript>