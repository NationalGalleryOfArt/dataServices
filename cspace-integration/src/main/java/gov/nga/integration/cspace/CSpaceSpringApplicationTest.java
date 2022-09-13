package gov.nga.integration.cspace;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtEntity.OperatingMode;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = CSpaceSpringApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT ) 
public class CSpaceSpringApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(RunAllTestsController.class);

    @Autowired
    ArtDataManagerService artDataManager;

    //public static void setArtDataManager(ArtDataManagerService newArtDataManager) {
    //    staticArtDataManager = newArtDataManager;
    //}

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
            "BOTH",		"/art/tms/objects/50724.json",  "", 													"200",			"\"lastModified\" : \"2020-02-06T22:01:29-05:00\"", "",
            "BOTH",		"/art/tms2/objects/1138.json", 	"", 													"404",			"",
            "BOTH",		"/art/objects/1138.json", 	  	"", 		 											"308", 			"",
            "BOTH",		"/art/objects/.json", 	  		"", 													"308", 			"Location", "objects.json?id=", "",
            "BOTH",		"/art/tms/objects/.json",  		"", 													"400", 			"",
            "PRIVATE",	"/art/tms/objects/93013.json", 	"",														"200",			"hasPrimaryDepiction", "id\" : \"94a36ec4-4110-454f-adaf-82236cf9c258", "",

            // specific fields expected for the various image classifications

            // cspace expects "Media Art" even though classification has been changed to "Time-Based Media Art"
            "BOTH",		"/art/tms/objects.json", 	  	"id=155535", 											"200",			"\"total\" : 1", 		"\"classification\" :",						"Media Art", "",

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
            "BOTH",		"/art/objects.json", 	  		"title=vehicle&order=id&limit=1", 					"200",			"!\"total\" : 0", 		"\"id\" : \"60345",	 						"",
            "PRIVATE",	"/art/objects.json", 	  		"title=vehicle&order=-cultObj:id&limit=1", 			"200",			"\"limit\" : 1", 		"\"id\" : \"216521",						"",
            "PUBLIC",	"/art/objects.json", 	  		"title=vehicle&order=-cultObj:id&limit=1", 			"200",			"\"limit\" : 1", 		"\"id\" : \"216521",						"",
            "PRIVATE",	"/art/objects.json", 	  		"title=buffalo&order=-cultObj:title", 					"200",			"\"total\" : 79", 		"",
            "PRIVATE",	"/art/objects.json", 	  		"title=buffalo&order=-cultObj:title&skip=2&limit=1", 	"200",			"\"total\" : 79", 		"\"title\" : \"[Water Buffalo in the Ganges River",			"/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHB",	"",
            "PRIVATE",	"/art/objects.json", 	  		"title=untitled&order=-artistNames,number&cultObj:artistNames=willis&skip=1&limit=1", 		
            "200",			"\"total\" : 10", 		"\"accessionNum\" : \"X.12827\"",					"!\"thumbnail\"",	"",
            "PRIVATE",	"/art/objects.json",			"number=19&skip=8000&limit=2000&thumbnails=0&references=0",
            "200",			"\"total\" : 10",		"\"limit\" : 1000",	"\"skip\" : 8000", "",
            "PRIVATE",	"/art/objects.json",			"id=102658&base64=0",									"200",			"(?s).*\"thumbnail\" : \"//(pixels|vm-imgrepo).*", "",
            "BOTH",		"/art/tms/objects.json",		"number=2016",											"200",			"\"1943.3.2016\"", "",
            "BOTH",		"/art/objects.json",			"id=119",												"200",			"\"predicate\" : \"hasDepiction\"", "222d2b5a-931b-4505-9bb8-6a57f54efd56", "",
            "PRIVATE",	"/art/objects.json",			"id=93013", 											"200",			"hasPrimaryDepiction", "id\" : \"94a36ec4-4110-454f-adaf-82236cf9c258", 	"!://[",	"",
            "PRIVATE",	"/art/objects.json", 			"artistNames=Hopper, Edward",							"200",			"total\" : 190",	"",		


            // SERVICE #3: IMAGE CONTENT
            "PRIVATE",	"/media/web-images-repository/images/94a36ec4-4110-454f-adaf-82236cf9c258", "",			"200",			"Content-Type: image/tiff",			"",
            "BOTH",		"/media/images/A9A25EA6-B078-43AC-A178-86681E56769A",	"",								"400",			"",
            //"PRIVATE",	"/media/portfolio-dclpa/images/2556",					"",								"200",			"image/x-adobe-dng", "",
            "BOTH",		"/media/images/2566",									"",								"400",			"",
            //"PRIVATE",	"/media/portfolio-dclpa/images/2774",					"",								"200",			"image/x-adobe-dng", "", // has space in the file name
            //"PRIVATE",	"/media/portfolio-dclpa/images/6820",					"",								"200",			"application/octet-stream", "",
            //"PRIVATE",	"/media/portfolio-dclpa/images/7007",					"",								"404",			"", 

            // SERVICE #4: IMAGE RECORD
            // since we have removed portfolio as a source of images, there's no longer a need to redirect - that's fancy, but not sure I agree with that approach generally due to inconsistencies
            "BOTH",		"/media/images/2556.json", 								"",								"308",			"Location", "media/images.json?id=2556", "",
            //"BOTH",		"/media/images/2556.json", 								"",								"404",			"",
            //"PRIVATE",		"/media/portfolio-dclpa/images/2556.json",				"",								"200",			"!\"references", "",
            "BOTH",		"/media/web-images-repository/images/94a36ec4-4110-454f-adaf-82236cf9c258.json", "",	"200",			"\"references", "lastModified", "fingerprint\" : \"", "",
            "BOTH",		"/media/nosuchsource/images/asdfasdf.json",				"",								"400",			"",
            "BOTH",		"/media/web-images-repository/images/234234.json",		"",								"404",			"",
            /*"PRIVATE",	"/media/portfolio-dclpa/images/3001.json",				"",								"200",			"!\"references",	"\"source\" : \"portfolio-dclpa", "classification\" : \"conservationImage\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/9721.json",				"",								"200",			"\"source\" : \"portfolio-dclpa", "classification\" : \"conservationImage\"", "\"treatmentPhase\"", "fingerprint\" : \"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/10162.json",				"",								"200",			"accessionNum\" : \"1992.108.1\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/9721.json",				"",								"200",			"\"lastModified\" : \"2015-10-26T00:00:00-04:00\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/5284.json",				"",								"200",			
        								"\"originalSource", "\"originalSourceInstitution", "\"originalSourceType", "\"originalFilename", "\"productType", "\"productionDate",  
        								"\"spectrum", "\"lightQuality", "\"viewDescription", "\"photographer", "\"creator", "\"captureDevice", "\"subjectWidthCM", "\"subjectHeightCM", 
        								"\"classification", "\"filename", "\"title", "\"source", "\"id\" : \"5284", "\"lastModified", "\"references", "id\" : \"53587\"", "\"depicts\"", "",
        "PRIVATE",	"/media/portfolio-dclpa/images/5004.json",				"",								"200",
        								"!\"references", "!\"subjectWidthCM", "\"originalSource\"", "\"originalSourceInstitution\"", "\"classification\" : \"conservationImage\"", "",
             */
            "PRIVATE",	"/media/web-images-repository/images/c058f43a-da89-4d06-95cb-da7ab5048c41.json", "",	"200", "predicate\" : \"primarilyDepicts\"", "viewType\" : \"primary\"", "\"5177\"", "!subjectWidthCM", "",
            "BOTH",		"/media/web-images-repository/images/5911893b-14aa-431c-85c8-6d19e6604192.json", "",	"200", "predicate\" : \"depicts\"", "\"119\"", "subjectWidthCM", "",

            // SERVICE #5: IMAGE RECORD SEARCH
            //"PRIVATE",	"/media/images.json",									"id=2566",						"200",			"\"references",			"\"thumbnail", "conservationImage", "",
            "PRIVATE",	"/media/images.json",									"id=5911893b-14aa-431c-85c8-6d19e6604192",						
            "200",			"\"references",			"\"thumbnail", "publishedImage", "\"viewType\"", "",
            "BOTH",		"/media/images.json",									"id=FEFA",						
            "200",			"total\" : 0", "",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json",					"id=2566",						"200",			"\"references",			"\"thumbnail", "",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json",					"image:id=2566",				"200",			"\"references",			"\"thumbnail", "",
            "BOTH",		"/media/web-images-repository/images.json", 			"id=2566",						"200",			"!\"references",		"!\"thumbnail", 	"\"total\" : 0", "",
            "PRIVATE",	"/media/images.json",									"cultObj:id=76219",				"200",			"\"references",	"fingerprint\" : \"",	"\"thumbnail", "\"total\" : 1", "",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json",					"cultObj:id=76219",				"200",			"\"references",	"fingerprint\" : \"",	"\"thumbnail", "\"total\" : 4", "",
            "BOTH",		"/media/web-images-repository/images.json",				"cultObj:id=76219",		 		"200",			"\"references",			"\"thumbnail", "\"total\" : 1", "",
            "BOTH",		"/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-title",
            "400",																	"",
            "BOTH",		"/media/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&limit=1",
            "200",			"b92ab9da-ec50-4b9e-9f7c-481e0d98f7dc", 				"",
            /*"PRIVATE",	"/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title&skip=4",
        																						"200",			"\"items\" : [ ]", 										"",
        "PRIVATE",	"/media/portfolio-dclpa/images.json",									"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&skip=9&limit=1",
        																						"200",																	"",
        "PRIVATE",	"/media/portfolio-dclpa/images.json", 					"cultObj:artistNames=Cezanne&cultObj:title=Cezanne&references=false&thumbnails=false&order=-cultObj:title,-image:id&limit=1",
        																						"200",			"\"id\" : \"3994", 										"",
        "PRIVATE",	"/media/portfolio-dclpa/images/2566.asdf",				"",								"404",			"",
             */
            "PRIVATE",	"/media/images.json", 									"cultObj:artistNames=gogh&skip=0&limit=25&references=false&thumbnails=false&order=image:id",
            "200",			"0ca8a5f5-f151-458e-a59b-a0d7dd84cde0", "1034a430-14b9-494d-8b69-c135e592892f", "!f512b066-86e6-471d-a870-c2891615dcf4", "",
            "PRIVATE",	"/media/images.json",									"id=d11ecc48-9cf8-482f-bf63-8ec3238816ef", "200", "image/tiff\"", 	"",
            "BOTH",		"/media/images.json",									"id=067027D2-37A5-443A-AD9B-9FD78CCEAC22", "200", "total\" : 0", 	"",
            "BOTH",		"/media/web-images-repository/images/067027D2-37A5-443A-AD9B-9FD78CCEAC22.json", "",			   "404", "",
            "BOTH",		"/media/images.json",									"id=e7aa77ac-3add-4abc-a978-691d1b2fa3b3", "200",			
            "\"filename", "\"title", "\"George Romney; 1937.1.105", "",
            //"PRIVATE",	"/media/images.json",									"id=3924",						"200",			
            //														"\"treatmentPhase", "\"spectrum", "\"lightQuality", "\"viewDescription", 
            //														"\"filename", "\"productionDate", "\"description", "!\"title", "fingerprint\" : \"", "",
            /*"PRIVATE",	"/media/images.json",									"id=1960",						"200",			
																"\"treatmentPhase", "\"spectrum", "\"lightQuality", "\"viewDescription", 
																"\"filename", "\"productionDate", "\"description", "\"title", 
																"\"Madame Dietz-Monnin", "\"Edgar Degas; 1951.2.1", "",
             */														
            "BOTH",		"/media/images.json", 	  								"lastModified=2016-04-05&lastModified=2016-06-04", 		"200",			"!\"total\" : 0", 		"\"total\" :", 		"",
            "BOTH",		"/media/web-images-repository/images.json", 	  		"lastModified=asdf2016-04-05&lastModified=2016-06-04", 	"400",			"not parse", 								"",
            "BOTH",		"/media/images.json", 	  								"lastModified=", 										"200",			"\"total\" : 0", 							"",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-04-05", 								"200",			"!\"total\" : 0", 		"",
            "BOTH",		"/media/images.json", 	  								"lastModified=2100-04-05", 								"200",			"\"total\" : 0", 		"",
            "BOTH",		"/media/images.json", 	  								"lastModified=2100-04-05&lastModified=", 				"200",			"\"total\" : 0", 		"",
            "BOTH",		"/media/images.json", 	  								"lastModified=2015-04-05&lastModified=", 				"200",			"!\"total\" : 0", 		"\"limit\" : 50",	"",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-06-04&lastModified=2015-06-04",		"200",			"!\"total\" : 0", 		"",
            //"PRIVATE",	"/media/portfolio-dclpa/images.json", 	  				"lastModified=2016-07-28&lastModified=2016-07-28",		"200",			"!\"total\" : 0", 		"",
            "BOTH",		"/media/images.json", 	  								"lastModified=&lastModified=2016-06-04",				"200",			"!\"total\" : 0", 		"",

            // SERvICE #6: ERROR LOGGER
            "BOTH",		"/system/logger.json",			"severity=error&origin=someurl&summary=maximum value for pageSize&details=The limit for page size is 25",
            "200",			"limit for page size", "error", ""

    };

    @Autowired
    private ServletWebServerApplicationContext server;

    //@GetMapping("/server-port")
    //public String serverPort() {
    //    return "" + server.getWebServer().getPort();
    //}

    public String getHostPort() {
        // String port = System.getProperty("server.port","");
        Integer p = server.getWebServer().getPort();
        String port = p.toString();
        if (!StringUtils.isNullOrEmpty(port))
            port = ":" + port;
        return "http://localhost" + port;
    }

    @Test
    public void testServerIsResponding() throws Exception {
        TestRestTemplate tmpl = new TestRestTemplate();
        HttpHeaders headers = tmpl.getForEntity(getHostPort(), String.class).getHeaders();
        for (Entry<String,List<String>> e : headers.entrySet()) {
            String k = e.getKey();
            for (String s : e.getValue()) {
                log.info(k + ":" + s);	
            }
        }
        // Assert.assertTrue(headers.get("Server").toString().equals("[Apache]"));
        String cstr = headers.get("Content-Length").get(0).toString();
        Long clen = Long.parseLong(cstr);
        Assert.assertTrue( clen > 0 );
    }

    @Test
    public void testAPIs() throws AssertionError {
        runAllTests(artDataManager, getHostPort() );
    }

    public static void runAllTests(ArtDataManagerService artDataManager, String hostPort) throws AssertionError {
        TestRestTemplate tmpl = new TestRestTemplate();
        Assert.assertTrue(testData.length > 0);
        AssertionError lastError = null;
        for (int i=0; i<testData.length; i++) {

            // collect data for the test
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

    private static boolean responseCodeValidates(ResponseEntity<String> resp, String code) {
        String respCode = "" + resp.getStatusCodeValue();
        return respCode.equals(code);
    }

    private static boolean contentValidates(ResponseEntity<String> resp, List<String> validation) {
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
            if (body != null && 
                    ( body.contains(v) || 
                            ( v.contains("|") && Pattern.matches(v, body) ) 
                    ) 
            ) {
                found = true;
            }

            for (Entry<String,List<String>> e : resp.getHeaders().entrySet()) {
                String k = e.getKey();
                for (String s : e.getValue()) {
                    String combined = k + ": " + s;

                    // use regular expression matcher instead of simple contains
                    if ( v.contains("|") ) {
                        if ( (k != null && Pattern.matches(v, k)) || (s != null && Pattern.matches(v, s)) || ( combined != null && Pattern.matches(v, combined)) ) {
                            found = true;
                            break;
                        }
                    }
                    else {
                        if ( (k != null && k.contains(v)) || (s != null && s.contains(v)) || ( combined != null && combined.contains(v)) ) {
                            found = true;
                            break;
                        }
                    }
                }
                if ( found )
                    break;
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

