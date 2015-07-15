component extends='org.lucee.cfml.test.LuceeTestCase' {

	function run() {

		describe( 'Youtube URL' , function() {

			it( 'works using http{}' , function() {

				http 
					method="get" 
					result="local.result" 
					url="https://www.googleapis.com/youtube/v3/search" 
					addtoken="false"
					{};

					//dump(SSLCertificateInstall("www.googleapis.com"));
				expect( result.statuscode ).toInclude( 400 );

			} );

			it( 'works using new http()' , function() {
				var httpService = new http();

				httpService.setMethod('get'); 
				httpService.setUrl('https://www.googleapis.com/youtube/v3/search');
				local.result = httpService.send().getPrefix();

				expect( result.statuscode ).toInclude( 400 );

			} );

		} );

		describe( 'URLs from LDEV-292' , function() {

			it( 'sni.velox.ch works' , function() {

				http 
					method="get" 
					result="local.result" 
					url="https://sni.velox.ch" 
					addtoken="false"
					{};
				expect( result.statuscode ).toInclude( 200 );

			});

			it( 'api.progresso.net works' , function() {

				http 
					method="get" 
					result="local.result" 
					url="https://api.progresso.net" 
					addtoken="false"
					{};
				expect( result.statuscode ).toInclude( 200 );

			});

			it( 'maps.googleapis.com works' , function() {

				http 
					method="get" 
					result="local.result" 
					url="https://maps.googleapis.com/maps/api/geocode/json" 
					addtoken="false"
					{};
				expect( result.statuscode ).toInclude( 200 );

			});

		} );
	
	}

}