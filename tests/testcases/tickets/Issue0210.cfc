
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	variables.StringEscapeUtils=createObject("java","org.apache.commons.lang.StringEscapeUtils");
	variables.url=createURL("Issue0210/Test.cfc");
	variables.expected='

			<br>hfd'&chr(132);


	variables.body='<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope 
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
	xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<soapenv:Body><ns1:test soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" 
		xmlns:ns1="http://Issue0210.tickets.testcases.tests"/>
	</soapenv:Body>
</soapenv:Envelope>';

	//public function setUp(){}

	public void function testCreateObjectWebservice(){
		var ws=createObject("webservice",variables.url&"?wsdl");
		assertEquals(variables.expected,ws.test());
	}

	public void function testHTTP(){
		http method="post" result="local.result" url="#variables.url#" addtoken="false" {
			
			// headers
			httpparam type="header" name="Accept" value="application/soap+xml, application/dime, multipart/related, text/*";
			httpparam type="header" name="Pragma" value="no-cache";
			httpparam type="header" name="SOAPAction" value='""';
			httpparam type="header" name="Content-Type" value="text/xml; charset=utf-8";
			//httpparam type="header" name="Content-Length" value="391";
			
			// body
			httpparam type="body" value="#variables.body#";
			// &amp;#x000a;&amp;#x000a;&amp;#x0009;&amp;#x0009;&amp;#x0009;hfd&#x84;
			// &amp;#x000a;&amp;#x000a;&amp;#x0009;&amp;#x0009;&amp;#x0009;hfd&#x84;
		}

		var res=result.filecontent;
		var start=find('<testReturn',res);
		start=find('>',res,start+1);
		var end=find('</testReturn>',res);
		var raw=mid(res,start+1,end-start-1)
		
		//dump(StringEscapeUtils.unescapeHtml(raw));
		//dump(StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(raw)));
		assertEquals(variables.expected,StringEscapeUtils.unescapeHtml(raw));
	}
	
	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
	
} 