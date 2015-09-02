component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe('LDEV-458 cfscript test component',function(){

			beforeEach( function() {
				actual_cfscript = GetMetadata( new LDEV0458.hasparams() ).properties;
			});

			it( 'has 3 properties' , function() {
				expect( actual_cfscript ).toBeArray();
				expect( actual_cfscript ).toHaveLength( 3 );
			});

			it( 'has expected first property' , function() {
				expect( actual_cfscript[1] ).toBe( {
					'type': 'any',
					'name': 'property1',
					'inject': ''
				} );
			});

			it( 'has expected second property' , function() {
				expect( actual_cfscript[2] ).toBe( {
					'type': 'any',
					'name': 'property2',
					'inject': 'property2'
				} );
			});

			it( 'has expected third property' , function() {
				expect( actual_cfscript[3] ).toBe( {
					'type': 'any',
					'name': 'property3',
					'inject': 'something_else'
				} );
			});


		});

		describe('LDEV-458 cfml test component',function(){

			beforeEach( function() {
				actual_cfml = GetMetadata( new LDEV0458.hasparamscfml() ).properties;
			});

			it( 'has 3 properties' , function() {
				expect( actual_cfml ).toBeArray();
				expect( actual_cfml ).toHaveLength( 3 );
			});

			it( 'has expected first property' , function() {
				expect( actual_cfml[1] ).toBe( {
					'type': 'any',
					'name': 'property1',
					'inject': ''
				} );
			});

			it( 'has expected second property' , function() {
				expect( actual_cfml[2] ).toBe( {
					'type': 'any',
					'name': 'property2',
					'inject': 'property2'
				} );
			});

			it( 'has expected third property' , function() {
				expect( actual_cfml[3] ).toBe( {
					'type': 'any',
					'name': 'property3',
					'inject': 'something_else'
				} );
			});


		});
	}
} 
