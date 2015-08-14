component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
		variables.queryWithDataIn = Query(
			id: [ 1 , 2 , 3 , 4 , 5 ]
		);
	}

	function run( testResults , testBox ) {

		describe('QueryExecute returns expected row',function(){

			describe( 'when params are specified as an array' , function() {
				it( 'with names' , function() {

					var actual = QueryExecute(
						params = [
							{ name: 'p1' , value: 1 , sqltype: 'integer' },
							{ name: 'p2' , value: 2 , sqltype: 'integer' },
							{ name: 'p3' , value: 3 , sqltype: 'integer' }
						],
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								:p1 AS v1,
								:p2 AS v2,
								:p3 AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});

				it( 'without names' , function() {

					var actual = QueryExecute(
						params = [
							{ value: 1 , sqltype: 'integer' },
							{ value: 2 , sqltype: 'integer' },
							{ value: 3 , sqltype: 'integer' }
						],
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								? AS v1,
								? AS v2,
								? AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});

				it( 'without names ( simple array )' , function() {

					var actual = QueryExecute(
						params = [ 1 , 2 , 3 ],
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								? AS v1,
								? AS v2,
								? AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});
			});

			describe( 'when params are specified as a struct' , function() {

				it( 'in correct case' , function() {
					var actual = QueryExecute(
						params = {
							'p1': { value: 1 , sqltype: 'integer' },
							'p2': { value: 2 , sqltype: 'integer' },
							'p3': { value: 3 , sqltype: 'integer' }
						},
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								:p1 AS v1,
								:p2 AS v2,
								:p3 AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});

				it( 'in incorrect case' , function() {
					var actual = QueryExecute(
						params = {
							'P1': { value: 1 , sqltype: 'integer' },
							'P2': { value: 2 , sqltype: 'integer' },
							'P3': { value: 3 , sqltype: 'integer' }
						},
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								:p1 AS v1,
								:p2 AS v2,
								:p3 AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});

				it( 'with simple values' , function() {
					var actual = QueryExecute(
						params = {
							'p1': 1,
							'p2': 2,
							'p3': 3
						},
						options = { dbtype: 'query' },
						sql = "
							SELECT 
								:p1 AS v1,
								:p2 AS v2,
								:p3 AS v3
							FROM queryWithDataIn
							WHERE id = 1
						"
					);

					verifyQuery( actual );
				});

			});

			describe( 'when params are passed in arguments' , function() {

				it( 'via argumentCollection' , function() {

					var testmethod = function(p2,p1,p3) {
						return QueryExecute(
							params = {
								'p1': 1,
								'p2': 2,
								'p3': 3
							},
							options = { dbtype: 'query' },
							sql = "
								SELECT 
									:p1 AS v1,
									:p2 AS v2,
									:p3 AS v3
								FROM queryWithDataIn
								WHERE id = 1
							"
						);
					};

					var actual = testmethod( argumentCollection = { 
						'p1': 1,
						'p2': 2,
						'p3': 3
					});
					verifyQuery( actual );
				});

				it( 'as name=value pair' , function() {
					
					var testmethod = function(p1,p2,p3) {
						return QueryExecute(
							params = arguments,
							options = { dbtype: 'query' },
							sql = "
								SELECT 
									:p1 AS v1,
									:p2 AS v2,
									:p3 AS v3
								FROM queryWithDataIn
								WHERE id = 1
							"
						);
					};

					var actual = testmethod( 
						'p1' = 1,
						'p2' = 2,
						'p3' = 3
					);
					verifyQuery( actual );
				});

				it( 'as named parameters without keys' , function() {
					
					var testmethod = function(p2,p1,p3) {
						return QueryExecute(
							params = arguments,
							options = { dbtype: 'query' },
							sql = "
								SELECT 
									:p1 AS v1,
									:p2 AS v2,
									:p3 AS v3
								FROM queryWithDataIn
								WHERE id = 1
							"
						);
					};

					var actual = testmethod( 2 , 1 , 3 );
					verifyQuery( actual );
				});

				it( 'as name=value pair when read into a structure' , function() {
					var testmethod = function(p1,p2,p3) {
						var p = {};
						for ( var k in arguments ) {
							p[k] = arguments[k];
						}

						return QueryExecute(
							params = p,
							options = { dbtype: 'query' },
							sql = "
								SELECT 
									:p1 AS v1,
									:p2 AS v2,
									:p3 AS v3
								FROM queryWithDataIn
								WHERE id = 1
							"
						);
					};
					var actual = testmethod( 1 , 2 , 3 );

					verifyQuery( actual );
				});

			});

		});

	}

	function verifyQuery( actual ) {
		expect( arguments.actual.RecordCount ).toBe( 1 );
		expect( arguments.actual.v1 ).toBe( 1 );
		expect( arguments.actual.v2 ).toBe( 2 );
		expect( arguments.actual.v3 ).toBe( 3 );
	}

	
	
} 
