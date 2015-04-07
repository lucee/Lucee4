<cfscript>
component extends="org.railo.cfml.test.RailoTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testValid(){
		var cfc=new CompValid();
		dump(cfc);
	}
} 
</cfscript>