component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
		variables.interestingNumbersAsAList = '3,4';
		variables.interestingStringsAsAList = "a,c,e";
		variables.interestingStringsAsAQuotedList = "'a','c','e'";

		variables.queryWithDataIn = Query(
			id: [ 1 , 2 , 3 , 4 , 5 ],
			value: [ 'a' , 'b' , 'c' , 'd' , 'e' ]
		);
	}

	function run( testResults , testBox ) {

		describe( 'query param order is correct' , function() {

			it( 'when all are specified as names' , function() {

				var actual = QueryExecute(
					params = [
						{ name: 'p1' , value: 1 , sqltype: 'integer' },
						{ name: 'p2' , value: 2 , sqltype: 'integer' },
						{ name: 'p3' , value: 3 , sqltype: 'integer' }
					],
					options = {
						dbtype: 'query'
					},
					sql = "
						SELECT 
							:p1 AS v1,
							:p2 AS v2,
							:p3 AS v3
						FROM queryWithDataIn
						WHERE id = 1
					"
				);

				expect( actual.RecordCount ).toBe( 1 );
				expect( actual.v1 ).toBe( 1 );
				expect( actual.v2 ).toBe( 2 );
				expect( actual.v3 ).toBe( 3 );

			});

			it( 'when all are specified as ?' , function() {

				var actual = QueryExecute(
					params = [
						{ value: 1 , sqltype: 'integer' },
						{ value: 2 , sqltype: 'integer' },
						{ value: 3 , sqltype: 'integer' }
					],
					options = {
						dbtype: 'query'
					},
					sql = "
						SELECT 
							? AS v1,
							? AS v2,
							? AS v3
						FROM queryWithDataIn
						WHERE id = 1
					"
				);

				expect( actual.RecordCount ).toBe( 1 );
				expect( actual.v1 ).toBe( 1 );
				expect( actual.v2 ).toBe( 2 );
				expect( actual.v3 ).toBe( 3 );

			});

			it( 'when mixed name,?,name' , function() {

				var actual = QueryExecute(
					params = [
						{ name: 'p1' , value: 1 , sqltype: 'integer' },
						{ name: 'p3' , value: 3 , sqltype: 'integer' },
						{ value: 2 , sqltype: 'integer' }
					],
					options = {
						dbtype: 'query'
					},
					sql = "
						SELECT 
							:p1 AS v1,
							? AS v2,
							:p3 AS v3
						FROM queryWithDataIn
						WHERE id = 1
					"
				);

				expect( actual.RecordCount ).toBe( 1 );
				expect( actual.v1 ).toBe( 1 );
				expect( actual.v2 ).toBe( 2 );
				expect( actual.v3 ).toBe( 3 );

			});

			it( 'when mixed ?,name,?' , function() {

				var actual = QueryExecute(
					params = [
						{ value: 1 , sqltype: 'integer' },
						{ value: 3 , sqltype: 'integer' },
						{ name: 'p2' , value: 2 , sqltype: 'integer' }
					],
					options = {
						dbtype: 'query'
					},
					sql = "
						SELECT 
							? AS v1,
							:p2 AS v2,
							? AS v3
						FROM queryWithDataIn
						WHERE id = 1
					"
				);

				expect( actual.RecordCount ).toBe( 1 );
				expect( actual.v1 ).toBe( 1 );
				expect( actual.v2 ).toBe( 2 );
				expect( actual.v3 ).toBe( 3 );

			});

		} );

		describe( 'selecting 2 rows from QoQ' , function() {

			describe( 'is possible using a hard coded list' , function() {

				it( 'of numerics' , function( currentSpec ) {

					var actual = QueryExecute(
						options = {
							dbtype: 'query'
						},
						sql = "
							SELECT 
								id,
								value
							FROM queryWithDataIn
							WHERE id IN ( "&interestingNumbersAsAList&" )
						"
					);

					expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' )  );

				});

				it( 'of strings' , function( currentSpec ) {

					var actual = QueryExecute(
						options = {
							dbtype: 'query'
						},
						sql = "
							SELECT 
								id,
								value
							FROM queryWithDataIn
							WHERE value IN ( "&interestingStringsAsAQuotedList&" )
						"
					);
					expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAQuotedList , ',' ) );

				});


			});

			describe( 'using param list=true' , function() {

				describe( 'with new Query()' , function() {

					beforeEach( function( currentSpec ) {
						q = new Query(
							dbtype = 'query',
							queryWithDataIn = variables.queryWithDataIn
						);
					});

					it( 'errors when you tell it you are using a list of numerics but are actually using a list of strings' , function() {

						q.addParam( name: 'needle' , value: interestingStringsAsAList , sqltype: 'numeric' , list: true );

						expect( function() {
							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE id IN ( :needle )
							" ).getResult();
						} ).toThrow( 'expression' );

					});

					describe( 'returns expected rows' , function() {

						it( 'when using numeric params' , function() {

							q.addParam( name: 'needle' , value: interestingNumbersAsAList , sqltype: 'numeric' , list: true );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE id IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using numeric params and a custom separator' , function() {

							q.addParam( name: 'needle' , value: Replace( interestingNumbersAsAList , ',' , '|' ) , sqltype: 'numeric' , list: true , separator: '|' );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE id IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using string params' , function() {

							q.addParam( name: 'needle' , value: interestingStringsAsAList , sqltype: 'varchar' , list: true );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE value IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});

					});

				});

				describe( 'with query{} ( cfquery )' , function() {

					it( 'errors when you tell it you are using a list of numerics but are actually using a list of strings' , function() {
						
						expect( function() {
							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( "
								);

								queryparam
									value = interestingStringsAsAList
									sqltype = 'integer'
									list = true;

								WriteOutput( " )" );
							}
						} ).toThrow( 'expression' );

					});


					describe( 'returns expected rows' , function() {

						it( 'when using numeric params' , function() {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( "
								);

								queryparam
									value = interestingNumbersAsAList
									sqltype = 'integer'
									list = true;

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using numeric params and a custom separator' , function() {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( "
								);

								queryparam
									value = Replace( interestingNumbersAsAList , ',' , '|' )
									sqltype = 'integer'
									list = true
									separator = '|';

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using string params' , function() {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( "
								);

								queryparam
									value = interestingStringsAsAList
									sqltype = 'varchar'
									list = true;

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});
					});
				});

				describe( 'with QueryExecute' , function() {

					it( 'errors when you tell it you are using a list of numerics but are actually using a list of strings' , function() {
						
						expect( function() {
							var actual = QueryExecute(
								params = [
									{ name: 'needle' , value: interestingStringsAsAList , sqltype: 'numeric' , list = true }
								],
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);
						} ).toThrow( 'expression' );

					});


					describe( 'returns expected rows' , function() {

						it( 'when using an array of numeric params' , function() {

							var actual = QueryExecute(
								params = [
									{ name: 'needle' , value: interestingNumbersAsAList , sqltype: 'numeric' , list = true }
								],
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});


						it( 'when using a struct of numeric params' , function() {

							var actual = QueryExecute(
								params = {
									needle: { value: interestingNumbersAsAList , sqltype: 'numeric' , list: true }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using an array of string params' , function() {

							var actual = QueryExecute(
								params = [
									{ name: 'needle' , value: interestingStringsAsAList , sqltype: 'varchar' , list = true }
								],
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});


						it( 'when using a struct of string params' , function() {

							var actual = QueryExecute(
								params = {
									needle: { value: interestingStringsAsAList , sqltype: 'varchar' , list: true }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});


						it( 'when using numeric params and a custom separator' , function() {

							var actual = QueryExecute(
								params = {
									needle: { value: Replace( interestingNumbersAsAList , ',' , '|' ) , sqltype: 'numeric' , list: true , separator: '|' }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

					});

				});

			});

		});


	}
	
	
} 