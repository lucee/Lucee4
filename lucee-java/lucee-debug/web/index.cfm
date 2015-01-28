<cfscript>
	sClearPassword = "Test-123!"&chr(196)&chr(196)&"s";
	sHashPassword1 = hash(input:sClearPassword);
	sHashPassword2 = hash40(sClearPassword);
	dump(sHashPassword1);
	dump(sHashPassword2);


	sHashPassword1 = hash(sClearPassword);
	sHashPassword2 = hash40(sClearPassword);
	dump(sHashPassword1);
	dump(sHashPassword2);
</cfscript>