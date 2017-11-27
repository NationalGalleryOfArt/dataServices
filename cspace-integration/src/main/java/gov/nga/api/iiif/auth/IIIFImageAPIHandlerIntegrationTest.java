package gov.nga.api.iiif.auth;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gov.nga.integration.cspace.CSpaceSpringApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = CSpaceSpringApplication.class)
@AutoConfigureMockMvc
public class IIIFImageAPIHandlerIntegrationTest {
	
	private static final Logger log = LoggerFactory.getLogger(IIIFImageAPIHandlerIntegrationTest.class);
 
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void iip_nosample_openaccess_image() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/objects/5/1/51-primary-0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:4217 5770")))
        	;
    }

    @Test
    public void iip_nosample_restricted_image() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/objects/6/1/61-primary-0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:438 500")))
        	;
    }

    // forces header in request to test that returned dimensions are full and correct for NGA
    @Test
    public void iip_sample_restricted_image() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/objects/6/1/61-primary-0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number")
        			.header("NGA_INTERNAL",true))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:3507 4000")))
        	;
    }

    @Test
    public void iiif_unsupported_quality() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/normal.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }

    @Test
    public void iiif_unsupported_rotation() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/10.2/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }

    @Test
    public void iiif_bogus_region() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/adffull/512,/0/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }

    @Test
    public void iiif_bogus_image() throws Exception {
        mvc.perform(get("/iiif/public/objedafcts/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg"))
        	.andExpect(status().isNotFound())
        	;
    }
    
    @Test
    public void iiif_nonzoom_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-740x560.jpg/full/512,/0/default.jpg"))
        	.andExpect(status().isNotFound())
        	;
    }
    
    @Test
    public void iiif_sample_openaccess_image_options() throws Exception {
        mvc.perform(options("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
           	.header("Access-Control-Request-Method", "GET")
           	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET,HEAD,POST"))
        	;
    }

    @Test
    public void iiif_sample_restricted_image() throws Exception {
        mvc.perform(get("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/full/512,/0/default.jpg")
           	.header("Access-Control-Request-Method", "GET")
           	.header("Origin","https://someserver.com"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.IMAGE_JPEG))
    		.andExpect(header().string("Access-Control-Allow-Origin","*"))
    		.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }

    @Test
    public void iiif_nosample_openaccess_image_options() throws Exception {
        mvc.perform(options("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
        	.header("Access-Control-Request-Method", "GET")
        	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET,HEAD,POST"))
        	;
    }

    @Test
    public void iiif_nosample_openaccess_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.IMAGE_JPEG))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }

    @Test
    public void iiif_nosample_restricted_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/6/1/61-primary-0-nativeres.ptif/full/512,/0/default.jpg"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/full/512,/0/default.jpg"))
        	;
    }
    
    @Test
    public void iiif_nosample_openaccess_image_to_infojson_redirect() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/info.json"))
        	;
    }

    @Test
    public void iiif_nosample_restricted_image_to_infojson_redirect() throws Exception {
        mvc.perform(get("/iiif/public/objects/6/1/61-primary-0-nativeres.ptif"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/info.json"))
        	;
    }
    
    @Test
    public void iiif_sample_openaccess_image_to_infojson_redirect() throws Exception {
        mvc.perform(get("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/info.json"))
        	;
    }

    @Test
    public void iiif_sample_restricted_image_to_infojson_redirect() throws Exception {
        mvc.perform(get("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/info.json"))
        	;
    }

    @Test
    public void iiif_nosample_openaccess_infojson() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/info.json"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.APPLICATION_JSON))
        	.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        	.andExpect(jsonPath("$.width", equalTo(4217)));
        	;
    }

    @Test
    public void iiif_nosample_restricted_infojson() throws Exception {
        mvc.perform(get("/iiif/public/objects/6/1/61-primary-0-nativeres.ptif/info.json"))
        	.andExpect(status().is(303))
        	.andExpect(redirectedUrl("/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/info.json"))
        	;
    }
    
    @Test
    public void iiif_sample_openaccess_infojson() throws Exception {
        mvc.perform(get("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/info.json"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.APPLICATION_JSON))
        	.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        	.andExpect(jsonPath("$.width", equalTo(263)))
        	;
    }
    
    @Test
    public void test_severed_connection_handling() throws Exception {
    	// HTTP GET request
    	String url = "http://localhost:8080/iiif/public/objects/6/1/61-primary-0-nativeres.ptif/full/800,/0/default.jpg";

    	URL obj = new URL(url);
    	HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    	// optional default is GET
    	con.setRequestMethod("GET");

    	int responseCode = con.getResponseCode();
    	log.debug("Sending 'GET' request to URL : " + url);
    	log.debug("Response Code : " + responseCode);
    	log.debug("content-type:" + con.getContentType());

    	BufferedReader in = new BufferedReader(
    			new InputStreamReader(con.getInputStream()));
    	String inputLine = in.readLine();
    	log.debug("received " + inputLine.length() + " bytes.  Severing connection now");

    	// close prematurely to cause an error 
    	con.disconnect();
    }
    
    @Test
    public void iiif_nosample_openaccess_region_outside_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/-10,-10,5,5/512,/0/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }

    @Test
    public void iiif_nosample_openaccess_region_tangential_to_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/-10,-10,10,50/512,/0/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }

    @Test
    public void iiif_nosample_openaccess_region_one_pixel_image() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/-10,-10,11,50/512,/0/default.jpg"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.IMAGE_JPEG))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }
    
    @Test
    public void iiif_nosample_openaccess_region_tangential_to_image_on_y() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/-10,-10,11,10/512,/0/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }
    
    @Test
    public void iiif_nosample_openaccess_region_one_pixel_image_on_x_and_y() throws Exception {
        mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/-10,-10,11,11/512,/0/default.jpg"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.IMAGE_JPEG))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }

}



