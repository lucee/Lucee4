component extends="org.lucee.cfml.test.LuceeTestCase" {

	function testNonExistentFile() {
		var filePathName = "/tmp/this_file_totes_doesnt_exist_baby.txt";
		var now = now();
		try {
		fileSetLastModified(filePathName, now);
		fail("fileSetLastModified should throw an error for non-existent files.");
		}
		catch(TestBox.AssertionFailed e){
			throw e;
		}
		catch(any e){}
	}
	function testExistentFile() {
		var filePathName = getCurrentTemplatePath();
		var now = now();
		fileSetLastModified(filePathName, now);
		
	}

}