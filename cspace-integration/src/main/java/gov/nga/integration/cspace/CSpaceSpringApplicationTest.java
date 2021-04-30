// TODO - convert this class to JUnit Tests (see IIIFImageAPIHandlerIntegrationTest for example)
/*
    NGA ART DATA API: CSpaceSpringApplicationTest provides a custom set of tests for the NGA data api.

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gov.nga.integration.cspace;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.OperatingMode;
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
	
    TestRestTemplate tmpl = new TestRestTemplate();
    
    @Before
    public void readyForTest() throws InterruptedException {
    	while (!artDataManager.isDataReady(false)) {
    		log.info("********* NOT READY FOR TESTING YET **************");
    		Thread.sleep(10000);
    	}
    }
    
    static final String[] testData = {
    	// url						  	DATA													RETURN CODE		CONTENT VALIDATION #1 (! means is not present)			CONTENT VALIDATION #2, 						END VALIDATIONS WITH EMPTY STRING
    		
    	// SERVICE #1 : art object record
    	"BOTH", 	"/art/tms/objects/1138.json", 	"", 													"200", 			"\"artistNames\" : \"Giovanni Bellini (painter);", 		"\"predicate\" : \"hasPrimaryDepiction\"",	"",
    	"BOTH", 	"/art/tms/objects/999999.json", "", 													"404",			"",
    	"BOTH",		"/art/tms/objects/50724.json",  "", 													"200",			"\"lastModified\" : \"2019-10-28T22:01:34-04:00\"", "",
    	"BOTH",		"/art/tms2/objects/1138.json", 	"", 													"404",			"",
    	"BOTH",		"/art/objects/1138.json", 	  	"", 		 											"308", 			"",
    	"BOTH",		"/art/objects/.json", 	  		"", 													"308", 			"Location", "objects.json?id=", "",
    	"BOTH",		"/art/tms/objects/.json",  		"", 													"400", 			"",
    	"PRIVATE",	"/art/tms/objects/93013.json", 	"",														"200",			"hasDepiction", "hasPrimaryDepiction", "id\" : \"6799", "",
    	
    	// TODO - add specific fields expected for the various image classifications
    	
    	// SERVICE #2: search for art objects
    	"BOTH",		"/art/tms/objects.json", 	  	"id=1138", 												"200",			"\"total\" : 1", 		"\"thumbnail\" :",							"/9j/4AAQSkZJRgABAgAAAQ", "",
    	"BOTH",		"/art/tms/objects.json", 	  	"id=1138&title=mud",									"200",			"\"total\" : 0", 		"", 
    	"BOTH",		"/art/tms/objects.json", 	  	"cultObj:id=1138", 										"200",			"\"total\" : 1", 		"fingerprint\" : \"", "\"thumbnail\" :",	"/9j/4AAQSkZJRgABAgAAAQ", "",
    	"BOTH",		"/art/objects.json", 	  	  	"id=1138", 												"200",			"\"total\" : 1", 		"\"thumbnail\" :",							"/9j/4AAQSkZJRgABAgAAAQ", "",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=2016-04-05&lastModified=2016-06-04", 		"200",			"!\"total\" : 0", 		"\"total\" :", 								"",
    	"BOTH",		"/art/tms/objects.json", 	  	"lastModified=asdf2016-04-05&lastModified=2016-06-04", 	"400",			"not parse", 			"",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=", 										"200",			"\"total\" : 0", 		"",
    	"BOTH",		"/art/tms/objects.json", 	  	"lastModified=2016-04-05", 								"200",			"!\"total\" : 0", 		"",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=2100-04-05", 								"200",			"\"total\" : 0", 		"",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=2100-04-05&lastModified=", 				"200",			"\"total\" : 0", 		"",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=2015-04-05&lastModified=", 				"200",			"!\"total\" : 0", 		"\"limit\" : 50",							"",
    	"BOTH",		"/art/tms/objects.json", 	  	"lastModified=2016-06-04&lastModified=2015-06-04",		"200",			"!\"total\" : 0", 		"",
    	"BOTH",		"/art/objects.json", 	  		"lastModified=&lastModified=2016-06-04",				"200",			"!\"total\" : 0", 		"",
    	"BOTH",		"/art/objects.json", 	  		"number=1992.51.9", 									"200",			"\"total\" : 72", 		"\"id\" : \"76219",	 						"\"references\" : [ {",		"thumbnail\" :", "",
    	"BOTH",		"/art/objects.json", 	  		"number=1992.51.9&references=0&thumbnails=0",			"200",			"\"total\" : 72", 		"\"id\" : \"76219",	 						"!references",				"!thumbnail",	"",
    	"BOTH",		"/art/objects.json", 	  		"title=sketchbook&order=id&limit=1", 					"200",			"!\"total\" : 0", 		"\"id\" : \"50763",	 						"",
    	"PRIVATE",	"/art/objects.json", 	  		"title=sketchbook&order=-cultObj:id&limit=1", 			"200",			"\"limit\" : 1", 		"\"id\" : \"212998",						"",
    	"PUBLIC",	"/art/objects.json", 	  		"title=sketchbook&order=-cultObj:id&limit=1", 			"200",			"\"limit\" : 1", 		"\"id\" : \"218908",						"",
    	"PRIVATE",	"/art/objects.json", 	  		"title=frog&order=-cultObj:title", 						"200",			"\"total\" : 64", 		"",
    	"PRIVATE",	"/art/objects.json", 	  		"title=frog&order=-cultObj:title&skip=2&limit=1", 		"200",			"\"total\" : 64", 		"\"title\" : \"Toy Bank: Frog\"",			"/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQ",	"",
    	"PRIVATE",	"/art/objects.json", 	  		"title=untitled&order=-artistNames,number&cultObj:artistNames=willis&skip=1&limit=1", 		
    																							"200",			"\"total\" : 10", 		"\"accessionNum\" : \"X.12827\"",					"!\"thumbnail\"",	"",
    	"PRIVATE",	"/art/objects.json",			"number=19&skip=8000&limit=2000&thumbnails=0&references=0",
        																						"200",			"\"total\" : 10",		"\"limit\" : 1000",	"\"skip\" : 8000", "",
        "PRIVATE",	"/art/objects.json",			"id=125133&base64=0",									"200",			"\"thumbnail\" : \"//", "",
        "BOTH",		"/art/tms/objects.json",		"number=2016",											"200",			"\"1943.3.2016\"", "",
        "BOTH",		"/art/objects.json",			"id=119",												"200",			"\"predicate\" : \"hasDepiction\"", "52A811A4-B499-4DF0-B4AF-17B5346F757E", "",
        "PRIVATE",	"/art/objects.json",			"id=93013", 											"200",			"hasDepiction",  "hasPrimaryDepiction", "id\" : \"6799", 	"!://[",	"",
        "PRIVATE",	"/art/objects.json", 			"artistNames=Hopper, Edward",							"200",			"total\" : 190",	"",		

        
        // SERVICE #3: IMAGE CONTENT
        "PRIVATE",	"/media/web-images-repository/images/AFD0E48D-E522-4B75-97FA-3E085D766642", "",			"200",			"image/tiff",			"",
        "BOTH",		"/media/images/A9A25EA6-B078-43AC-A178-86681E56769A",	"",								"400",			"",
        // "PRIVATE",	"/media/portfolio-dclpa/images/2556",					"",								"200",			"image/x-adobe-dng", "",
        "BOTH",		"/media/images/2566",									"",								"400",			"",
        //"PRIVATE",	"/media/portfolio-dclpa/images/2774",					"",								"200",			"image/x-adobe-dng", "", // has space in the file name
        //"PRIVATE",	"/media/portfolio-dclpa/images/6820",					"",								"200",			"application/octet-stream", "",
        //"PRIVATE",	"/media/portfolio-dclpa/images/7007",					"",								"404",			"", 
        
        // SERVICE #4: IMAGE RECORD
        // "BOTH",		"/media/images/AF0BDE80-23AA-4045-A5B4-D395B94C0EB7.json", 								"",								"308",			"Location", "media/images.json?id=AF0BDE80-23AA-4045-A5B4-D395B94C0EB7", "",
        //"PRIVATE",		"/media/portfolio-dclpa/images/2556.json",				"",								"200",			"!\"references", "",
        "BOTH",		"/media/web-images-repository/images/AF0BDE80-23AA-4045-A5B4-D395B94C0EB7.json", "",	"200",			"\"references", "lastModified", "fingerprint\" : \"", "",
        "BOTH",		"/media/nosuchsource/images/asdfasdf.json",				"",								"400",			"",
        "BOTH",		"/media/web-images-repository/images/234234.json",		"",								"404",			"",
        "PRIVATE",	"/media/portfolio-dclpa/images/3001.json",				"",								"200",			"!\"references",	"\"source\" : \"portfolio-dclpa", "classification\" : \"conservationImage\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/9721.json",				"",								"200",			"\"source\" : \"portfolio-dclpa", "classification\" : \"conservationImage\"", "\"treatmentPhase\"", "fingerprint\" : \"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/10162.json",				"",								"200",			"accessionNum\" : \"1992.108.1\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/9721.json",				"",								"200",			"\"lastModified\" : \"2015-10-26T00:00:00-04:00\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/5284.json",				"",								"200",			
        								"\"originalSource", "\"originalSourceInstitution", "\"originalSourceType", "\"originalFilename", "\"productType", "\"productionDate",  
        								"\"spectrum", "\"lightQuality", "\"viewDescription", "\"photographer", "\"creator", "\"captureDevice", "\"subjectWidthCM", "\"subjectHeightCM", 
        								"\"classification", "\"filename", "\"title", "\"source", "\"id\" : \"5284", "\"lastModified", "\"references", "id\" : \"53587\"", "\"depicts\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/5004.json",				"",								"200",
        								"!\"references", "!\"subjectWidthCM", "\"originalSource\"", "\"originalSourceInstitution\"", "\"classification\" : \"conservationImage\"", "",
        "PRIVATE",	"/media/web-images-repository/images/1EA96663-F651-41C0-A35C-5CBF5EE22970.json", "",	"200", "predicate\" : \"primarilyDepicts\"", "viewType\" : \"primary\"", "\"5177\"", "!subjectWidthCM", "",
        "BOTH",		"/media/web-images-repository/images/52A811A4-B499-4DF0-B4AF-17B5346F757E.json", "",	"200", "predicate\" : \"depicts\"", "\"119\"", "subjectWidthCM", "",
        
        // SERVICE #5: IMAGE RECORD SEARCH
        "PRIVATE",	"/media/images.json",									"id=2566",						"200",			"\"references",			"\"thumbnail", "conservationImage", "",
        "PRIVATE",	"/media/images.json",									"id=06C7F451-E12D-470A-8DA7-99CD7D8ED41B",						
        																						"200",			"\"references",			"\"thumbnail", "publishedImage", "\"viewType\"", "",
        "BOTH",		"/media/images.json",									"id=FEFA",						
        																						"200",			"total\" : 0", "",
        "PRIVATE",	"/media/portfolio-dclpa/images.json",					"id=2566",						"200",			"\"references",			"\"thumbnail", "",
        "PRIVATE",	"/media/portfolio-dclpa/images.json",					"image:id=2566",				"200",			"\"references",			"\"thumbnail", "",
        "BOTH",		"/media/web-images-repository/images.json", 			"id=2566",						"200",			"!\"references",		"!\"thumbnail", 	"\"total\" : 0", "",
        "PRIVATE",	"/media/images.json",									"cultObj:id=76219",				"200",			"\"references",	"fingerprint\" : \"",	"\"thumbnail", "\"total\" : 5", "",
        "PRIVATE",	"/media/portfolio-dclpa/images.json",					"cultObj:id=76219",				"200",			"\"references",	"fingerprint\" : \"",	"\"thumbnail", "\"total\" : 4", "",
        "BOTH",		"/media/web-images-repository/images.json",				"cultObj:id=76219",		 		"200",			"\"references",			"\"thumbnail", "\"total\" : 1", "",
        "BOTH",		"/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-title",
        																						"400",																	"",
        "BOTH",		"/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&limit=1",
        																						"200",			"AF0BDE80-23AA-4045-A5B4-D395B94C0EB7", 				"",
        "PRIVATE",	"/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&skip=4",
        																						"200",			"\"items\" : [ ]", 										"",
        "PRIVATE",	"/media/portfolio-dclpa/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&skip=9&limit=1",
        																						"200",																	"",
        "PRIVATE",	"/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&limit=1",
        																						"200",			"\"id\" : \"3994", 										"",
        "PRIVATE",	"/media/portfolio-dclpa/images/2566.asdf",				"",								"404",			"",
        "PRIVATE",	"/media/images.json", 									"cultObj:artistNames=gogh&skip=71&limit=25&references=false&thumbnails=false&order=image:id",
        																						"200",			"EB31B934-FF88-4D73-9C6E-3CBFB2248427", "E7F34278-89BC-4120-BB15-8980561AEC6A", "!E7E24A15-8A07-483E-AE2F-A902DC6F1F14", "",
        "PRIVATE",	"/media/images.json",									"id=E7F34278-89BC-4120-BB15-8980561AEC6A", "200", "image/tiff\"", 	"",
        "BOTH",		"/media/images.json",									"id=EFA14652-51B8-4294-806A-18C3F6130026", "200", "total\" : 0", 	"",
        "BOTH",		"/media/web-images-repository/images/EFA14652-51B8-4294-806A-18C3F6130026.json", "",			   "404", "",
        "BOTH",		"/media/images.json",									"id=C63D03DA-A8F2-410E-9719-7ABE33D92778", "200",			
																"\"filename", "\"title", "\"George Romney; 1937.1.105", "",
		"PRIVATE",	"/media/images.json",									"id=3924",						"200",			
																"\"treatmentPhase", "\"spectrum", "\"lightQuality", "\"viewDescription", 
																"\"filename", "\"productionDate", "\"description", "!\"title", "fingerprint\" : \"", "",
		"PRIVATE",	"/media/images.json",									"id=1960",						"200",			
																"\"treatmentPhase", "\"spectrum", "\"lightQuality", "\"viewDescription", 
																"\"filename", "\"productionDate", "\"description", "\"title", 
																"\"Madame Dietz-Monnin", "\"Edgar Degas; 1951.2.1", "",
																
		"BOTH",		"/media/images.json", 	  								"lastModified=2016-04-05&lastModified=2016-06-04", 		"200",			"!\"total\" : 0", 		"\"total\" :", 		"",
		"BOTH",		"/media/web-images-repository/images.json", 	  		"lastModified=asdf2016-04-05&lastModified=2016-06-04", 	"400",			"not parse", 								"",
		"BOTH",		"/media/images.json", 	  								"lastModified=", 										"200",			"\"total\" : 0", 							"",
		"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-04-05", 								"200",			"!\"total\" : 0", 		"",
		"BOTH",		"/media/images.json", 	  								"lastModified=2100-04-05", 								"200",			"\"total\" : 0", 		"",
		"BOTH",		"/media/images.json", 	  								"lastModified=2100-04-05&lastModified=", 				"200",			"\"total\" : 0", 		"",
		"BOTH",		"/media/images.json", 	  								"lastModified=2015-04-05&lastModified=", 				"200",			"!\"total\" : 0", 		"\"limit\" : 50",	"",
		"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-06-04&lastModified=2015-06-04",		"200",			"!\"total\" : 0", 		"",
		"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-07-28&lastModified=2016-07-28",		"200",			"!\"total\" : 0", 		"",
		"BOTH",		"/media/images.json", 	  								"lastModified=&lastModified=2016-06-04",				"200",			"!\"total\" : 0", 		"",
		
        // SERvICE #6: ERROR LOGGER
		"BOTH",		"/system/logger.json",			"severity=error&origin=someurl&summary=maximum value for pageSize&details=The limit for page size is 25",
        																						"200",			"limit for page size", "error", ""
    
    };
    
    public String getHostPort() {
    	String port = System.getProperty("server.port","");
    	if (!StringUtils.isNullOrEmpty(port))
    		port = ":" + port;
    	return "http://localhost" + port;
    }
    
	@Test
	public void testServerIsResponding() throws Exception {
		HttpHeaders headers = tmpl.getForEntity(getHostPort(), String.class).getHeaders();
		for (Entry<String,List<String>> e : headers.entrySet()) {
			String k = e.getKey();
			for (String s : e.getValue()) {
				log.info(k + ":" + s);	
			}
		}
		Assert.assertTrue(headers.get("Server").toString().equals("[Apache]"));
	}
	
	@Test
	public void testAPIs() throws AssertionError {
		String hostPort = getHostPort();
		Assert.assertTrue(testData.length > 0);
		AssertionError lastError = null;
		for (int i=0; i<testData.length; i++) {

			// collection our data for the test
			String env = testData[i++];
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
			
			// if we're operating in public mode and have a private test, then skip it and likewise for tests only relevant to public
			if ( artDataManager.getOperatingMode() == OperatingMode.PUBLIC && env.equals("PRIVATE"))
				continue;
			if ( artDataManager.getOperatingMode() == OperatingMode.PRIVATE && env.equals("PUBLIC"))
				continue;

			try {
			if (method == null || method.equals("G")) {
				// USING GET METHOD
				log.info("Testing GET Method to: " + gurl);
				ResponseEntity<String> resp = tmpl.getForEntity(hostPort+gurl, String.class);
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
			catch (AssertionError ae) {
				log.error("Assertion error:" + ae.getMessage());
				lastError = ae;
			}
		}
		if (lastError != null)
			throw lastError;
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
			String presence = finding ? "presence" : "non-presence";
			log.info(status + " validated " + presence + " of content: " + v);
			valid = valid && ok;
			
		}
		return valid;
	}
	

}

