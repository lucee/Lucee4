<cfset url.img=trim(url.img)>
<cfparam name="url.adminType" default="web" />
<cfset request.adminType = url.adminType />

<!--- user has to be logged in --->
<cfif not structKeyExists(session, "password#url.adminType#")>
	<cfthrow message="You need to be logged in" />
</cfif>

<!--- url.img must match an image for an extension --->
<cfif url.img neq "">
	<cfset found = false />
	<!--- Check if the url.img is set in one of the installed extensions --->
	<cfadmin
		action="getExtensions"
		type="#url.adminType#"
		password="#session['password#url.adminType#']#"
		returnVariable="extensions" />
	<cfloop query="extensions">
		<cfif extensions.image eq url.img>
			<cfset found = true />
			<cfbreak />
		</cfif>
	</cfloop>

	<!--- Now check if it is among the uninstalled extensions, if the url.img is a valid URL --->
	<cfif not found and isValid('url', url.img)>
		<cfinclude template="extension.functions.cfm" />
		<cfset data = loadAllProvidersData(timeout=1000) />

		<!--- loop through all extension providers, check their extension images --->
		<cfloop collection="#data#" item="cfcData" index="key">
			<cfif not isStruct(cfcData) or not structKeyExists(cfcData, "listApplications")>
				<cfcontinue />
			</cfif>
			<cfloop query="cfcData.listApplications">
				<cfif cfcData.listApplications.image eq url.img>
					<cfset found = true />
					<cfbreak />
				</cfif>
			</cfloop>
			<cfif found>
				<cfbreak />
			</cfif>
		</cfloop>
	</cfif>
	<!--- Image not found? Bad bad bad --->
	<cfif not found>
		<cfthrow message="The image you wanted to view, cannot be found in any of the extensions" detail="img=#url.img#" />
	</cfif>
</cfif>

<!--- go to a non-default application scope, for storing the image cache data --->
<cfapplication name='__LUCEE_STATIC_CONTENT' sessionmanagement='false' clientmanagement='false'
				applicationtimeout='#createtimespan( 1, 0, 0, 0 )#'>
	
	<cfsetting showdebugoutput="no">
	<cfparam name="url.width" default="80">
	<cfparam name="url.height" default="40">
	<cfset id=hash(url.img&"-"&url.width&"-"&url.height)>
	<cfset mimetypes={png:'image/png',gif:'image/gif',jpg:'image/jpeg'}>
	
	<cfif len(url.img) ==0>
		<cfset ext="gif"><!--- using tp.gif in that case --->
	<cfelse>
	    <cfset ext=listLast(url.img,'.')>
	</cfif>

	<!--- check for valid file extension --->
	<cfif not structKeyExists(mimetypes, ext)>
		<cfthrow message="Invalid request for file [#url.img#]" />
	</cfif>
		
	<cfheader name='Expires' value='#getHttpTimeString( now() + 100 )#'>
	<cfheader name='Cache-Control' value='max-age=#86400 * 100#'>	
	<cfset etag=hash(id)>	
	<cfheader name='ETag' value='#etag#'>

	<!--- copy and shrink to local dir --->
	<cfset tmpfile=expandPath("{temp-directory}/admin-ext-thumbnails/"&id&"."&ext)>	
	<cfif fileExists(tmpfile)>
		<!--- etag matches, return 304 !--->
		<cfif len( CGI.HTTP_IF_NONE_MATCH ) && ( CGI.HTTP_IF_NONE_MATCH == '#etag#' )>
			<cfheader statuscode='304' statustext='Not Modified'>
			<cfcontent reset='#true#' type='#mimetypes[ext]#'><cfabort>
		</cfif>
		<!--- No eTag given? Read the tmp image for processing --->
		<cffile action="readbinary" file="#tmpfile#" variable="data">
	<cfelseif len(url.img) ==0>
		<cfset data=toBinary("R0lGODlhMQApAIAAAGZmZgAAACH5BAEAAAAALAAAAAAxACkAAAIshI+py+0Po5y02ouz3rz7D4biSJbmiabqyrbuC8fyTNf2jef6zvf+DwwKeQUAOw==")>
	<cfelse>
		<!--- requested file does not exist? --->
		<cfif not fileExists(url.img)>
			<cfthrow message="Image requested does not exist (img=#url.img#)" />
		<!--- check if the file is within the Lucee directories, if it is a local file --->
		<cfelseif not isValid('url', url.img)>
			<cfset checkPath = replace(url.img, '\', '/', 'all') />
			<cfif findNoCase(replace(expandPath('{lucee-web}'), '\', '/', 'all'), checkPath) neq 1
			  and findNoCase(replace(expandPath('{lucee-server}'), '\', '/', 'all'), checkPath) neq 1>
				<cfthrow message="Image requested is not within the web/server context directory (img=#url.img#)" />
			</cfif>
		</cfif>

		<cffile action="readbinary" file="#url.img#" variable="data">

		<!--- base64 encoded binary --->
		<!--- PK: not used in the admin anywhere, so removed.
			  (code would have crashed anyway at the file extension check above)
		<cfelse>
			<cftry>
				<cfset data=toBinary(url.img)>
				<cfcatch><cfset systemOutput(e,true,true)></cfcatch>
			</cftry>
		</cfif>
		--->
	</cfif>

	<cfimage action="read" source="#data#" name="img">

	<!--- shrink images if needed --->
	<cfif img.height GT url.height or img.width GT url.width>
		<cfif img.height GT url.height >
			<cfimage action="resize" source="#img#" height="#url.height#" name="img">
		</cfif>
		<cfif img.width GT url.width>
			<cfimage action="resize" source="#img#" width="#url.width#" name="img">
		</cfif>
		<cfset data=toBinary(img)>
	</cfif>

	<cftry>
		<cffile action="write" file="#tmpfile#" output="#data#" createPath="true">
		<cfcatch><cfrethrow></cfcatch><!--- if it fails because there is no permission --->
	</cftry>

	<cfcontent reset="yes" type="#mimetypes[ext]#" variable="#data#">