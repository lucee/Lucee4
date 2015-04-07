<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testWorks(){
		new abstract.Direct();
		new abstract.InDirect();
	}

	public void function testCannotInstantiateInterface(){
		var notFailed=false;
		try{
			new abstract.Interface();
			notFailed=true;
		}
		catch(e){
			assertTrue(find("the interface [",e.message));
			assertTrue(find("] cannot be used as a component.",e.message));
		}
		if(notFailed)fail("cannot instantiate a interface");
	}

	public void function testCannotInstantiateAbstractComponent(){
		var notFailed=false;
		try{
			new abstract.Abs();
			notFailed=true;
		}
		catch(e){
			assertTrue(find("you cannot instantiate the abstract component [",e.message));
		}
		if(notFailed)fail("cannot instantiate a abstract component");
	}

	public void function testCannotExtendFinalComponent(){
		var notFailed=false;
		try{
			new abstract.CannotExtendFinalComponent();
			notFailed=true;
		}
		catch(e){
			assertTrue(find("you cannot extend the final component [",e.message));
		}
		if(notFailed)fail("cannot extend a final component");
	}

	public void function testAbstractFunctionsOnlyInAbstractComponents(){
		var notFailed=false;
		try{
			new abstract.AbstractFunctionsOnlyInAbstractComponents();
			notFailed=true;
		}
		catch(e){
			assertTrue(find("is not allowed within the no abstract component",e.message));
		}
		if(notFailed)fail("abstract functions only can be in abstract components");
	}


	public void function testAbstractFunction(){
		var notFailed=false;
		try{
			new abstract.AbstractFunction();
			notFailed=true;
		}
		catch(e){
			assertTrue(find("does not implement the function",e.message));
		}
		if(notFailed)fail("non abstract component that not implement a abstract function");
	}

	public void function testOverwriteFinalMethod(){
		var notFailed=false;
		try{
			dump(new abstract.OverwriteFinalMethod1());
			notFailed=true;
		}
		catch(e){
			assertTrue(find("tries to overwrite a final method",e.message));
		}
		if(notFailed)fail("overwrite final method1");


		var notFailed=false;
		try{
			dump(new abstract.OverwriteFinalMethod2());
			notFailed=true;
		}
		catch(e){
			assertTrue(find("tries to overwrite a final method",e.message));
		}
		if(notFailed)fail("overwrite final method2");
	}
/*


	// 
	try{
		dump(var:new Invalid3(),label:"should not work");
	}
	catch(e){
		dump(var:e.message,label:"non abstract component that not implement a abstract function");
	}


*/
} 
</cfscript>