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
 --->
			if ( typeof obj == 'string' || obj instanceof String )
				return document.getElementById( obj );

			return obj;
		}

		var selectText = function( obj ) {

	        if ( document.selection ) {

	            var range = document.body.createTextRange();
	            range.moveToElementText( getDomObject( obj ) );
	            range.select();
	        } else if ( window.getSelection ) {

	            var range = document.createRange();
	            range.selectNode( getDomObject( obj ) );
	            window.getSelection().addRange( range );
	        }
	    }


		$( function(){

			$( '.coding-tip-trigger-#request.adminType#' ).click( 
				function(){ 
					var $this = $(this);
					$this.next( '.coding-tip-#request.adminType#' ).slideDown();
					$this.hide();
				}
			);

			$( '.coding-tip-#request.adminType# code' ).click( 
				function(){ 					
					selectText(this);					
				}
			).prop("title", "Click to select the text");
		});
	</script>

	<cfif isDefined( "Request.htmlBody" )>#Request.htmlBody#</cfif>
</body>
</html>
</cfoutput>
	<cfset thistag.generatedcontent="">
</cfif>

<cfparam name="url.showdebugoutput" default="no">
<cfsetting showdebugoutput="#url.showdebugoutput#">