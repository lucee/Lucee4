<cfscript>
// password for the Lucee Web Admin
request.webAdminPassword="server";

// addional testcase directories
request.external={
	//"Memcached":"/Users/mic/Projects/Extensions/Memcached/tests/",
	//"MongoDB":"/Users/mic/Projects/Extensions/MongoDB/tests/"
};

// Test Extensions (need to be installed)
request.testJBossExtension=false;
request.testMongoDBExtension=false;
request.testMemcachedExtension=true;



// Datasources
request.mysql="mysql";
</cfscript>