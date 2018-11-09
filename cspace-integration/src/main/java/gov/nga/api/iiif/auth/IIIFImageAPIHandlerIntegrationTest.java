/*
    NGA IIIF Authentication API Implementation: Integration Testing Module for NGA IIIF Auth 

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
package gov.nga.api.iiif.auth;


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.integration.cspace.CSpaceSpringApplication;
import static gov.nga.utils.CaseInsensitiveSubstringMatcher.containsStringCaseInsensitive;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = CSpaceSpringApplication.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IIIFImageAPIHandlerIntegrationTest {
	
	private static final Logger log = LoggerFactory.getLogger(IIIFImageAPIHandlerIntegrationTest.class);
 
	@Autowired
	ArtDataManagerService adms;
	
    @Autowired
    private MockMvc mvc;
    
    @Test // this works well
    public void _first_Test() throws InterruptedException {
    	while (adms == null || !adms.isDataReady(false)) {
    		Thread.sleep(3000);
    	}
    }

    @Test
    public void iip_long_path_can_access_image_test() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/research/italian_paintings_13th_14th_centuries/objects/1/9/8/4/2/3/198423-compfig-4.0-nativeres.ptif&SDS=0,90&JTL=1,1")
           	.header("Access-Control-Request-Method", "GET")
           	.header("Origin","https://someserver.com"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.IMAGE_JPEG))
    		.andExpect(header().string("Access-Control-Allow-Origin","*"))
    		.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }

    @Test
    public void iip_nosample_openaccess_research_image() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/research/dutch_paintings_17th_century/objects/6/0/60-technical-4.1-nativeres.ptif&SDS=0,90&JTL=1,1")
           	.header("Access-Control-Request-Method", "GET")
           	.header("Origin","https://someserver.com"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.IMAGE_JPEG))
    		.andExpect(header().string("Access-Control-Allow-Origin","*"))
    		.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }
    	
    @Test
    public void iip_nosample_openaccess_research_image_metadata() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/research/dutch_paintings_17th_century/objects/6/0/60-technical-4.0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:1934 2433")))
        	;
    }

    @Test
    public void iiif_bad_image_request_should_not_give_link_to_cspace() throws Exception {
        mvc.perform(get("/iiif/boogeywoogey"))
        	.andExpect(status().isNotFound())
        	.andExpect(content().string(not(containsStringCaseInsensitive("cspace"))))
        	;
    }
    
    @Test
    public void fastcgi_bad_request_should_not_give_link_to_cspace() throws Exception {
        mvc.perform(get("/fastcgi/boogeywoogey"))
        	.andExpect(status().isNotFound())
        	//.andExpect(content().string(containsString("IIP Protocol")))
        	;
    }
    
    @Test
    public void iip_nosample_openaccess_image_metadata() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/objects/5/1/51-primary-0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:4217 5770")))
        	;
    }

    // on localhsot default case is external unless NGA_INTERAL header is set; however, to support this vis-a-vis integration with a live image server, we 
    // have to specify NGA_EXTERNAL => true otherwise the response from the server will be an internal one for the actual bit values since the web server
    // knows we're inside the firewall
    @Test
    public void iip_nosample_restricted_image() throws Exception {
        mvc.perform(get("/fastcgi/iipsrv.fcgi?FIF=/public/objects/6/1/61-primary-0-nativeres.ptif&obj=IIP,1.0&obj=Max-size&obj=Tile-size&obj=Resolution-number")
        	.header("NGA_EXTERNAL",  true))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/vnd.netfpx"))
        	.andExpect(header().string("Access-Control-Allow-Origin","*"))
        	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	.andExpect(content().string(containsString("Max-size:322 367")))
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
        	.andExpect(content().string(containsString("Max-size:10321 11771")))
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
        // mvc.perform(get("/iiif/public/objects/5/1/51-primary-0-740x560.jpg/full/512,/0/default.jpg"))
        mvc.perform(get("/iiif/public/manifests/nga_highlights.json/full/512,/0/default.jpg"))
        	.andExpect(status().isBadRequest())
        	;
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void iiif_sample_openaccess_image_options_get() throws Exception {
        mvc.perform(options("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
           	.header("Access-Control-Request-Method", "GET")
           	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().stringValues("Access-Control-Allow-Methods",hasItems(containsString("GET"))))
        	;
    }

    @Test
    public void iiif_sample_openaccess_research_image_options_get() throws Exception {
    	mvc.perform(get("/iiif/640/public/research/italian_paintings_13th_14th_centuries/objects/1/9/8/4/2/3/198423-compfig-4.0-nativeres.ptif/full/128,/0/default.jpg")
    		.header("Access-Control-Request-Method", "GET")
    		.header("Origin","https://someserver.com"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(MediaType.IMAGE_JPEG))
    		.andExpect(header().string("Access-Control-Allow-Origin","*"))
    		.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
    		;
    }

    @SuppressWarnings("unchecked")
	@Test
    public void iiif_sample_openaccess_image_options_head() throws Exception {
        mvc.perform(options("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
           	.header("Access-Control-Request-Method", "HEAD")
           	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().stringValues("Access-Control-Allow-Methods",hasItems(containsString("HEAD"))))
        	;
    }

    @SuppressWarnings("unchecked")
	@Test
    public void iiif_sample_openaccess_image_options_post() throws Exception {
        mvc.perform(options("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
           	.header("Access-Control-Request-Method", "POST")
           	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().stringValues("Access-Control-Allow-Methods",hasItems(containsString("POST"))))
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

    @SuppressWarnings("unchecked")
	@Test
    public void iiif_nosample_openaccess_image_options() throws Exception {
        mvc.perform(options("/iiif/public/objects/5/1/51-primary-0-nativeres.ptif/full/512,/0/default.jpg")
        	.header("Access-Control-Request-Method", "GET")
        	.header("Origin","https://someserver.com"))
        	.andExpect(status().isOk())
        	.andExpect(header().string("Access-Control-Allow-Origin","https://someserver.com"))
        	.andExpect(header().stringValues("Access-Control-Allow-Methods",hasItems(containsString("GET"))))
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
        	.andExpect(content().contentType("application/ld+json"))
        	//.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
    public void iiif_private_image_should_defer_to_iip_server() throws Exception {
        mvc.perform(get("/iiif/private/objects/1/6/7/8/8/2/167882-primary-0-nativeres.ptif/full/!100,100/0/default.jpg"))
    	.andExpect(status().isOk())
    	.andExpect(content().contentType(MediaType.IMAGE_JPEG))
    	.andExpect(header().string("Access-Control-Allow-Origin","*"))
    	.andExpect(header().string("Access-Control-Allow-Methods","GET, POST"))
        	;
    }
    
    @Test
    public void iiif_sample_openaccess_infojson() throws Exception {
        mvc.perform(get("/iiif/640/public/objects/5/1/51-primary-0-nativeres.ptif/info.json"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/ld+json"))
        	//.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
    
    @Test
    public void iiif_online_validator_unescaped_id_gives_400_or_404() throws Exception {
        mvc.perform(get("/iiif/public/[frob]/full/full/0/default.jpg"))
        	.andExpect(status().is(anyOf(is(400),is(404))))
        	;
    }

	@Test
    public void iiif_online_validator_random_format_gives_400_415_or_503() throws Exception {
        mvc.perform(get("/iiif/public/objects/0/0-primary-0-nativeres.ptif/full/full/0/default.AZ["))
        	.andExpect(status().is(anyOf(is(400),is(415),is(503))))
        	;
    }

	@Test
    public void iiif_online_validator_base_uri_redirect_produces_redirect() throws Exception {
		// shortest object image path
        mvc.perform(get("/iiif/public/objects/0/0-primary-0-nativeres.ptif"))
    	.andExpect(status().is(303))
    	.andExpect(redirectedUrl("/iiif/public/objects/0/0-primary-0-nativeres.ptif/info.json"))
       	;
        // and even shorter path to a test image
        mvc.perform(get("/iiif/public/iiif_validator/api_test.ptif"))
    	.andExpect(status().is(303))
    	.andExpect(redirectedUrl("/iiif/public/iiif_validator/api_test.ptif/info.json"))
       	;

	}

    @Test
    public void iiif_online_validator_content_type_for_descriptive_resource() throws Exception {
        mvc.perform(get("/iiif/public/iiif_validator/api_test.ptif/info.json"))
        	.andExpect(status().isOk())
        	.andExpect(content().contentType("application/ld+json"))
        	// .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        	;
    }

}



