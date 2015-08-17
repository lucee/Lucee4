
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run(){

		describe( 'elvis operator' , function() {

			it( 'returns "false" when first operand is a variable containing false' , function() {

				first_operand = false;
				actual = first_operand ?: 'foo';

				expect( actual ).toBe( false );

			});

			it( 'returns "false" when first operand is an inline false' , function() {

				actual = false ?: 'foo';

				expect( actual ).toBe( false );

			});

			it( 'returns "true" when first operand is an equality' , function() {

				actual = (1==1) ?: 'foo';

				expect( actual ).toBe( false );

			});

		});

	}

}