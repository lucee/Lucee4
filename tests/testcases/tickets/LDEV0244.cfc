
component extends="org.lucee.cfml.test.LuceeTestCase"	{

    function run(){

        describe( 'component' , function() {

            it( 'can be initiated' , function() {

                actual = new LDEV0244.good();

                    expect( actual ).toBe( 'hi' );

            });

            it( 'can be initiated even with a comment at the end' , function() {

                actual = new LDEV0244.bad();

                    expect( actual ).toBe( 'hi' );

            });

        });

    }

}