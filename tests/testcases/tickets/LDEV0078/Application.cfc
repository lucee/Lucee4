component{

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	


	/* HSQLDB

	this.datasources ={ 
 		"test":{
	  		class: 'org.hsqldb.jdbcDriver', 
	  		connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasources/db'}
	};
	this.datasource	=	"test";
	*/


 	this.datasource = {
		  class: 'org.gjt.mm.mysql.Driver'
		, connectionString: 'jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&tinyInt1isBit=true&autoReconnect=true&jdbcCompliantTruncation=true&useLegacyDatetimeCode=true'
		, username: 'root'
		, password: "encrypted:37ec58153dfa38c0860e2f9d3e13ea78e1ad53ea14dff4d1"
		
		// optional settings
		, connectionLimit:100 // default:-1
		, connectionTimeout:2 // default: 1; unit: seconds
		, timezone:'Europe/London'
		, storage:true // default: false
	};

	
	

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		//dialect = "MySQLwithInnoDB",
		autoManageSession = false,
		flushAtRequestEnd = false
	};

	function onRequestStart(){
		setting showdebugOutput=false;
		// init the table used
		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (
	`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
	`name` varchar(50) DEFAULT NULL,
	PRIMARY KEY (`id`)
	)");
		}
		query {
	        echo("INSERT INTO `test` VALUES ('1', null);");
		}
	}
	


}