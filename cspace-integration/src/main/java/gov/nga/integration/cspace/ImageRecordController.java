/*
    NGA ART DATA API: ImageRecordController maps and coordinates the specific URL requests for image records

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

import java.io.IOException;
import java.net.URI;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.OperatingModeService;
import gov.nga.integration.controllers.RecordSearchController;
import gov.nga.common.entities.art.Derivative;
import gov.nga.common.search.SearchHelper;
import gov.nga.common.search.SearchHelper.SEARCHOP;
import gov.nga.utils.StringUtils;

@RestController
public class ImageRecordController {
	
	@Autowired
	private ImageSearchController imgCtrl;
	
	@Autowired
	private CSpaceTestModeService ts;

	@Autowired
	private OperatingModeService om;

	private static final Logger log = LoggerFactory.getLogger(ImageRecordController.class);
    
    @RequestMapping(value="/media/images/{id}.json",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<RecordContainer> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	return imageRecordSource(null, id, request, response);
    }

    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    // IMAGE CONTENT SERVICE
    @RequestMapping(value="/media/{source}/images/{id}.json",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<RecordContainer> imageRecordSource(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	
    	log.debug("SOURCE: " + source);
    	
    	// if the source is not specified or too many sources are specified, then redirect to the generic search for image records service
    	String [] sourceScope = imgCtrl.getSources(request); 
    	if (sourceScope.length != 1) {
    		try {
    			return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(new URI(response.encodeRedirectURL("/media/images.json?id="+id))).body(null);
    		}
    		catch (Exception ue) {
    			log.error("Unexpected exception with URI: " + ue.getMessage());
    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    		}
    	}
 
    	// this service REQUIRES an ID - respond with bad request if ID is not supplied
    	if (StringUtils.isNullOrEmpty(id))
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// search the specified image source to find the record we're interested in 
    	SearchHelper<CSpaceImage> dSearchHelper = new SearchHelper<CSpaceImage>();
    	dSearchHelper.addFilter(Derivative.SEARCH.IMAGEID, SEARCHOP.EQUALS, id);
    	List<CSpaceImage> images = imgCtrl.searchImages(sourceScope, dSearchHelper, null);

    	if (images == null || images.size() != 1)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    	
    	// if we have gotten to this point, then we found the unique image and can construct an appropriate response
    	ImageRecord ir = new ImageRecord(images.get(0), true, om, ts, images, RecordSearchController.getRequestingServer(request));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		RecordSearchController.logSearchResults(request, 1);
		 
		return new ResponseEntity<RecordContainer>(new RecordContainer(ir), headers, HttpStatus.OK);
	}
    
    // IMAGE CONTENT SERVICE
    // catch the case where someone requests the content of an image without specifying the source.  We don't redirect in this case, we just
    // return 400 error
    @RequestMapping(value="/media/images/{id:.+}",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<InputStreamResource> imageContentNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException, APIUsageException, InterruptedException, ExecutionException {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // IMAGE CONTENT SERVICE
    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    @RequestMapping(value="/media/{source}/images/{id:.+}",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<InputStreamResource> imageContent(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {

    	// if the source is not specified or too many sources are specified, then redirect to the generic search for image records service
    	String [] sourceScope = imgCtrl.getSources(request); 
    	if (sourceScope.length != 1) {
   			log.error("A single source must be specified in the URL for image content: ");
   			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	}
 
    	if (StringUtils.isNullOrEmpty(id))
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// search the specified image source to find the record we're interested in 
    	SearchHelper<CSpaceImage> dSearchHelper = new SearchHelper<CSpaceImage>();
    	dSearchHelper.addFilter(Derivative.SEARCH.IMAGEID, SEARCHOP.EQUALS, id);
    	List<CSpaceImage> images = imgCtrl.searchImages(sourceScope, dSearchHelper, null);
    	
    	if (images == null || images.size() != 1)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	CSpaceImage d = images.get(0);
    	URI imageURI = d.getSourceImageURI("https");
    	
		RecordSearchController.logSearchResults(request, 1);
		
		try {
	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(d.getFormat().getMimetype()))
	                .body(new InputStreamResource(imageURI.toURL().openStream()));
		} 
		// return 404 if the source file cannot be found or accessed
		catch (IOException ie) {
			log.warn("IO Exception trying to access image record",ie);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

    
}