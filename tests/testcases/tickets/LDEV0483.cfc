component extends="org.lucee.cfml.test.LuceeTestCase" {

	function testLeapSwitch() {
		try {
			setTimeZone("Europe/London")
			local.illegalTime = createDateTime(2016, 3, 27, 1, 01, 00);
			fail("should throw an exception because this is not a valid time!");
		}
		catch(TestBox.AssertionFailed e){
			throw e;
		}
		catch(any e){}
	}

}