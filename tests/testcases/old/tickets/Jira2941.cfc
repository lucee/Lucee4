<cfscript>
component extends="org.railo.cfml.test.RailoTestCase"	{
	// static constructor
	static {
		private static.staticConstr1="static-constr-1";
		staticConstr2="static-constr-2";
		staticConstr3=function (abc,def){
			insideClosure=true;
			return static;
		}
		function staticConstr4(ghj){
			insideUDF=true;
			return variables;
		}
	}

	// static variables set outsite 
	static.constr1="constr-1";

	//public function setUp(){}


	public void function testStaticConstructorData(){
		var KEYS="CONSTR1,INSIDECLOSURE,INSIDEUDF,STATICCONSTR1,STATICCONSTR2,STATICCONSTR3,STATICCONSTR4";
		assertEquals("static-constr-1",Jira2941::staticConstr1);
		assertEquals("static-constr-2",Jira2941::staticConstr2);
		assertEquals("static-constr-1",static.staticConstr1);
		assertEquals("static-constr-2",static.staticConstr2);

		assertEquals(true,isClosure(Jira2941::staticConstr3));
		var res=Jira2941::staticConstr4();
		assertEquals(
			KEYS
			,listSort(structKeyList(Jira2941::staticConstr3()),"textNoCase"));


		assertEquals(
			KEYS
			,listSort(structKeyList(res),"textNoCase"));

		assertEquals(
			KEYS
			,listSort(structKeyList(static.staticConstr4()),"textNoCase"));
		
	}

	private void function test(){
		http method="get" result="local.result" url="#createURL("JiraXXXX/index.cfm")#" addtoken="false";
		/*
		assertEquals("",result.filecontent);
		
		try{
			// error
			fail("");
		}
		catch(local.exp){}*/
	}
	
	/*private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}*/
	
} 
</cfscript>