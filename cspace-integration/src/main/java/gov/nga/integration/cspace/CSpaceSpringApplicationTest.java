package gov.nga.integration.cspace;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map.Entry;

public class CSpaceSpringApplicationTest {

	private static final Logger log = LoggerFactory.getLogger(RunAllTestsController.class);

	private static ArtDataManagerService artDataManager;

	public static void setArtDataManager(ArtDataManagerService artDataManager) {
		CSpaceSpringApplicationTest.artDataManager = artDataManager;
	}
	
    RestTemplate tmpl = new TestRestTemplate();
    
    @Before
    public void readyForTest() throws InterruptedException {
    	while (!artDataManager.isDataReady(false)) {
    		log.info("********* NOT READY FOR TESTING YET **************");
    		Thread.sleep(10000);
    	}
    }
    
    static final String hostPort = "http://localhost:8100";
    static final String[] testData = {
    	// url						  	DATA													RETURN CODE		CONTENT VALIDATION #1 (! means is not present)			CONTENT VALIDATION #2, 						END VALIDATIONS WITH EMPTY STRING
    		
    	// SERVICE #1 : art object record
    	"/art/tms/objects/1138.json", 	"", 													"200", 			"\"artistNames\" : \"Giovanni Bellini (artist);", 		"\"predicate\" : \"hasPrimaryDepiction\"",	"",
    	"/art/tms/objects/999999.json", "", 													"404",			"",
    	"/art/tms2/objects/1138.json", 	"", 													"404",			"",
    	"/art/objects/1138.json", 	  	"", 													"308", 			"",
    	"/art/objects/.json", 	  		"", 													"400", 			"",
    	
    	// SERVICE #2: search for art objects
    	"/art/tms/objects.json", 	  	"id=1138", 												"200",			"\"total\" : 1", 		"\"thumbnail\" :",							"K05mypvXlSJLHUbaytboTR5", "",
    	"/art/tms/objects.json", 	  	"id=1138&title=mud",									"200",			"\"total\" : 0", 		"",
    	"/art/tms/objects.json", 	  	"cultObj:id=1138", 										"200",			"\"total\" : 1", 		"\"thumbnail\" :",							"K05mypvXlSJLHUbaytboTR5", "",
    	"/art/objects.json", 	  	  	"id=1138", 												"200",			"\"total\" : 1", 		"\"thumbnail\" :",							"K05mypvXlSJLHUbaytboTR5", "",
    	"/art/objects.json", 	  		"lastModified=2016-04-05&lastModified=2016-06-04", 		"200",			"!\"total\" : 0", 		"\"total\" :", 								"",
    	"/art/tms/objects.json", 	  	"lastModified=asdf2016-04-05&lastModified=2016-06-04", 	"400",			"not parse", 			"",
    	"/art/objects.json", 	  		"lastModified=", 										"200",			"\"total\" : 0", 		"",
    	"/art/tms/objects.json", 	  	"lastModified=2016-04-05", 								"200",			"!\"total\" : 0", 		"",
    	"/art/objects.json", 	  		"lastModified=2100-04-05", 								"200",			"\"total\" : 0", 		"",
    	"/art/objects.json", 	  		"lastModified=2100-04-05&lastModified=", 				"200",			"\"total\" : 0", 		"",
    	"/art/objects.json", 	  		"lastModified=2015-04-05&lastModified=", 				"200",			"!\"total\" : 0", 		"\"limit\" : 50",							"",
    	"/art/tms/objects.json", 	  	"lastModified=2016-06-04&lastModified=2015-06-04",		"200",			"!\"total\" : 0", 		"",
    	"/art/objects.json", 	  		"lastModified=&lastModified=2016-06-04",				"200",			"!\"total\" : 0", 		"",
    	"/art/objects.json", 	  		"number=1992.51.9", 									"200",			"\"total\" : 1", 		"\"id\" : 76219",	 						"\"references\" : [ {",		"thumbnail\" :", "\"total\" : 72", 	"",
    	"/art/objects.json", 	  		"number=1992.51.9&references=0&thumbnails=0",			"200",			"\"total\" : 1", 		"\"id\" : 76219",	 						"!references",				"!thumbnail",	"\"total\" : 72",	"",
    	"/art/objects.json", 	  		"title=sketchbook&order=id&limit=1", 					"200",			"!\"total\" : 0", 		"\"id\" : 50763",	 						"",
    	"/art/objects.json", 	  		"title=sketchbook&order=-cultObj:id&limit=1", 			"200",			"\"limit\" : 1", 		"\"id\" : 206294",	 						"",
    	"/art/objects.json", 	  		"title=frog&order=-cultObj:title", 						"200",			"\"total\" : 62", 		"",
    	"/art/objects.json", 	  		"title=frog&order=-cultObj:title&skip=2&limit=1", 		"200",			"\"total\" : 62", 		"\"title\" : \"Toy Bank: Frog\"",			"jcSOv616NXD/EnT7YafDqbbhLvETY6EYY5/T/OKLG1GSUt",	"",
    	"/art/objects.json", 	  		"title=untitled&order=-artistNames,number&cultObj:artistNames=willis&skip=1&limit=1", 		
    																							"200",			"\"total\" : 10", 		"\"number\" : \"X.12827\"",					"!\"thumbnail\"",	"",
        "/art/objects.json",			"number=19&skip=8000&limit=2000&thumbnails=0&references=0",
        																						"200",			"\"total\" : 10",		"\"limit\" : 1000",	"\"skip\" : 8000", "",
        "/art/objects.json",			"id=125133&base64=0",									"200",			"\"thumbnail\" : \"//vm-imgrepo-tdp\"", "",
        
        // SERVICE #3: IMAGE CONTENT
        "/media/web-images-repository/images/2C5EE199-D447-43F5-BB93-D2896EBB6483", "",			"200",			"\"image/tiff",			"",
        "/media/images/A9A25EA6-B078-43AC-A178-86681E56769A",	"",								"400",			"",
        "/media/images/portfolio-dclpa/2556",					"",								"200",			"\"image/x-adobe-dng", "",
        "/media/images/2566",									"",								"400",			"",
        
        // SERVICE #4: IMAGE RECORD
        "/media/images/2556.json", 								"",								"308",			"Location:/media/images.json?id=2556", "",
        "/media/portfolio-dclpa/images/2556.json",				"",								"200",			"!\"references", "",
        "/media/web-images-repository/images/0A78FF5D-1053-47AD-BA3D-D1C2AF4D4ADD.json", "",	"200",			"\"references", "lastModified", "",
        "/media/images/nosuchsource/asdfasdf.json",				"",								"400",			"",
        "/media/images/web-images-repository/234234.json",		"",								"404",			"",
        
        // SERVICE #5: IMAGE RECORD SEARCH
        "/media/images.json",									"id=2566",						"200",			"\"references",			"\"thumbnail", "",
        "/media/portfolio-dclpa/images.json",					"id=2566",						"200",			"\"references",			"\"thumbnail", "",
        "/media/portfolio-dclpa/images.json",					"image:id=2566",				"200",			"\"references",			"\"thumbnail", "",
        "/media/web-images-repository/images.json", 			"id=2566",						"200",			"!\"references",		"!\"thumbnail", 	"\"total\" : 0", "",
        "/media/images.json",									"cultObj:id=76219",				"200",			"\"references",			"\"thumbnail", "\"total\" : 5", "",
        "/media/portfolio-dclpa/images.json",					"cultObj:id=76219",				"200",			"\"references",			"\"thumbnail", "\"total\" : 4", "",
        "/media/web-images-repository/images.json",				"cultObj:id=76219",				"200",			"\"references",			"\"thumbnail", "\"total\" : 1", "",
        "/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-title",
        																						"400",																	"",
        "/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&limit=1",
        																						"200",			"0A78FF5D-1053-47AD-BA3D-D1C2AF4D4ADD", 				"",
        "/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&skip=4",
        																						"200",			"\"items\" : [ ]", 										"",
        "/media/portfolio-dclpa/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&skip=9&limit=1",
        																						"200",																	"",
        "/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&limit=1",
        																						"200",			"\"id\" : 3994", 										"",
        "/media/portfolio-dclpa/images/2566.asdf",												"404",			"",
        "/media/images.json", 									"cultObj:artistNames=gogh&skip=27&limit=25&references=false&thumbnails=false&order=image:id",
        																						"200",			"EB31B934-FF88-4D73-9C6E-3CBFB2248427", "F655F232-ACFD-4D2B-8CDE-43BD6C78B425", "!E7F34278-89BC-4120-BB15-8980561AEC6A", "!0B839226-9EC8-4DAE-945D-7330736FEB0A", "",
        
        // SERvICE #6: ERROR LOGGER
        "/system/logger.json",			"severity=error&origin=someurl&summary=maximum value for pageSize&details=The limit for page size is 25",
        																						"200",			"limit for page size", "error", ""
    
    };
    
	@Test
	public void testServerIsResponding() throws Exception {
		HttpHeaders headers = tmpl.getForEntity("http://localhost:8100", String.class).getHeaders();
		for (Entry<String,List<String>> e : headers.entrySet()) {
			String k = e.getKey();
			for (String s : e.getValue()) {
				log.info(k + ":" + s);	
			}
		}
		Assert.assertTrue(headers.get("Server").toString().contains("Apache-Coyote"));
	}
	
	@Test
	public void testAPIs() throws Exception {
		Assert.assertTrue(testData.length > 0);
		for (int i=0; i<testData.length; i++) {

			// collection our data for the test
			String gurl = testData[i++];
			String method = null;
			if (gurl.startsWith("G") || gurl.startsWith("P")) {
				method = gurl.substring(0, 1);
				gurl = gurl.substring(1,gurl.length());
			}
			String purl = gurl;
			String qs = testData[i++];

			// process query string to create GET QUERY STRING and POST params
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			if (!StringUtils.isNullOrEmpty(qs)) {
				// construct the GET URL
				gurl += "?" + qs;

				// assign the post params
				for (String p : qs.split("&")) {
					String[] vals = p.split("=");
					String k = vals[0];
					String v = "";
					if (vals.length > 1)
						v = vals[1];
					params.add(k, v);
				}
			}

			String code = testData[i++];
			List<String> validation = CollectionUtils.newArrayList();
			while (!StringUtils.isNullOrEmpty(testData[i])) {
				validation.add(testData[i++]);
			}

			if (method == null || method.equals("G")) {
				// USING GET METHOD
				log.info("Testing GET Method to: " + gurl);
				ResponseEntity<String> resp = tmpl.getForEntity(hostPort+gurl, String.class);
				Assert a = null;
				assertTrue(hostPort + gurl + " did not have code " + code, responseCodeValidates(resp, code));
				assertTrue(hostPort+gurl + " did not pass validation " + validation, contentValidates(resp, validation));
			}

			if (method == null || method.equals("P")) {
				// USING POST METHOD
				log.info("Testing POST Method to: " + purl + " with params " + params.toString());
				ResponseEntity<String> resp = tmpl.postForEntity(hostPort+purl, params, String.class);
				assertTrue(purl + params.toString() + " did not have code " + code,  responseCodeValidates(resp, code));
				assertTrue(purl + params.toString() + " did not pass validation " + validation,  contentValidates(resp, validation));
			}
		}
	}
	
	private boolean responseCodeValidates(ResponseEntity<String> resp, String code) {
		String respCode = resp.getStatusCode().toString();
		return respCode.equals(code);
	}

	private boolean contentValidates(ResponseEntity<String> resp, List<String> validation) {
		boolean valid = true;
		// check the body and headers for all of the expected 
		for (String v : validation) {

			boolean found = false;
			boolean finding = true;

			if (v.substring(0,1).equals("!")) {
				v = v.substring(1,v.length());
				finding = false;
			}
			
			String body = resp.getBody();
			if (body != null && body.contains(v)) {
				found = true;
			}

			for (Entry<String,List<String>> e : resp.getHeaders().entrySet()) {
				String k = e.getKey();
				for (String s : e.getValue()) {
					if ( (k != null && k.contains(v)) || (s != null && s.contains(v)) ) {
						found = true;
					}
				}
			}
			
			boolean ok = (finding && found) || (!finding && !found);
			String status = ok ? "Successfully" : "Unsuccessfully";
			String presence = finding ? "prescence" : "non-presence";
			log.info(status + " validated " + presence + " of content: " + v);
			valid = valid && ok;
			
		}
		return valid;
	}
	

}

