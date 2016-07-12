package gov.nga.integration.cspace;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.Derivative;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.utils.StringUtils;

@RestController
public class ImageRecordController {
	
	@Autowired
	private ImageSearchController imgCtrl;
	
	@Autowired
	private CSpaceTestModeService ts;
	
	private static final Logger log = LoggerFactory.getLogger(ImageRecordController.class);
    
    @RequestMapping("/media/images/{id}.json")
    public ResponseEntity<RecordContainer> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {
    	return imageRecordSource(null, id, request, response);
    }

    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    // IMAGE CONTENT SERVICE
    @RequestMapping("/media/{source}/images/{id}.json")
    public ResponseEntity<RecordContainer> imageRecordSource(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {
    	
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
    	ImageRecord ir = new ImageRecord(images.get(0), true, ts, imgCtrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		RecordSearchController.logSearchResults(request, 1);
		 
		return new ResponseEntity<RecordContainer>(new RecordContainer(ir), headers, HttpStatus.OK);
	}
    
    // IMAGE CONTENT SERVICE
    // catch the case where someone requests the content of an image without specifying the source.  We don't redirect in this case, we just
    // return 400 error
    @RequestMapping("/media/images/{id:.+}")
    public ResponseEntity<InputStreamResource> imageContentNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException, APIUsageException, InterruptedException, ExecutionException {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // IMAGE CONTENT SERVICE
    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    @RequestMapping("/media/{source}/images/{id:.+}")
    public ResponseEntity<InputStreamResource> imageContent(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException, APIUsageException, InterruptedException, ExecutionException {

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
    	
    	// TODO - enhance logging of all queries since requests for records is not being logged in the integration log currently

    	if (images == null || images.size() != 1)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	CSpaceImage d = images.get(0);
    	URL imageURL = null;
		imageURL = new URL("https:" + d.getSourceImageURI().toString());
		
		RecordSearchController.logSearchResults(request, 1);
		
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.getFormat().getMimetype()))
                .body(new InputStreamResource(imageURL.openStream()));
		
	}

    
}