component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
		variables.queryWithDataIn = Query(
			id: [ 1 , 2 , 3 , 4 , 5 ]
		);
	}

	function run( testResults , testBox ) {

		describe( 'REMatch' , function(){

			it( 'dot can match newline' , function() {

				actual = REMatch( '.',Chr( 10 ) );
				expect( actual ).toBe( [ Chr( 10 ) ] );

			});

		});

		describe( 'REMatchNoCase' , function(){

			it( 'dot can match newline' , function() {

				actual = REMatchNoCase( '.',Chr( 10 ) );
				expect( actual ).toBe( [ Chr( 10 ) ] );

			});

		});

		describe( 'REFind' , function() {

			it( 'dot can match newline' , function() {

				actual = REFind( '.',Chr( 10 ) );
				expect( actual ).toBe( 1 );

			});

		});

		describe( 'REFindNoCase' , function() {

			it( 'dot can match newline' , function() {

				actual = REFindNoCase( '.',Chr( 10 ) );
				expect( actual ).toBe( 1 );

			});

		});

	}
	
	
} 