component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe( '%' , function(){

			it( 'returns 0 for 0..10 % 1' , function() {

				for ( var i = 0 ; i <= 10 ; i++ ) {

					expect( i % 1 ).toBe( 0 );

				}

			});

			it( 'returns 1 for 5 % 2' , function() {

				expect( 5 % 2 ).toBe( 1 );

			});

			it( 'errors for 5 % 0' , function() {

				expect( function() {
					actual = 5 % 0;
				} ).toThrow(
					'java.lang.ArithmeticException',
					'Division by zero is not possible'
				);

			});

		});


		describe( 'MOD' , function(){

			it( 'returns 0 for 0..10 MOD 1' , function() {

				for ( var i = 0 ; i <= 10 ; i++ ) {

					expect( i MOD 1 ).toBe( 0 );

				}

			});

			it( 'returns 1 for 5 MOD 2' , function() {

				expect( 5 MOD 2 ).toBe( 1 );

			});

			it( 'errors for 5 MOD 0' , function() {

				expect( function() {
					actual = 5 MOD 0;
				} ).toThrow(
					'java.lang.ArithmeticException',
					'Division by zero is not possible'
				);

			});

		});

		describe( 'Division' , function(){

			it( 'errors for 5 / 0' , function() {

				expect( function() {
					actual = 5 / 0;
				} ).toThrow(
					'java.lang.ArithmeticException',
					'Division by zero.'
				);

			});

		});

	}
	
	
} 