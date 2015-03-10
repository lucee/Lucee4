<cfscript>
// password for the Lucee Web Admin
request.webAdminPassword="server";

// addional testcase directories
request.pathes=[];

// Test Extensions (need to be installed)
request.testJBossExtension=false;
request.testMongoDBExtension=false;
request.testMemcachedExtension=true;



// Datasources
request.mysql="mysql";
</cfscript>