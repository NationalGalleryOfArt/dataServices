package gov.nga.integration.cspace;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

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

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Derivative;
import gov.nga.utils.StringUtils;

@RestController
public class ImageRecordController {

	private static final Logger log = LoggerFactory.getLogger(ImageRecordController.class);
    
    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping("/media/images/{id}.json")
    public ResponseEntity<RecordContainer> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) {
    	return imageRecordSource(null, id, request, response);
    }

    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    @RequestMapping("/media/{source}/images/{id}.json")
    public ResponseEntity<RecordContainer> imageRecordSource(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) {
    	
    	log.debug("SOURCE: " + source);
    	// not implemented if the source is not specified OR (preferably) a redirect to the generic search for image records
    	if (source == null) {
    		try {
    			return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(new URI(response.encodeRedirectURL("/media/images.json?id="+id))).body(null);
    		}
    		catch (Exception ue) {
    			log.error("Unexpected exception with URI: " + ue.getMessage());
    			return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    		}
    	}
 
    	// not found if the source != the web images repository (at least for now)
    	if (!source.equals(ArtObjectImage.defaultSource))
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	if (StringUtils.isNullOrEmpty(id))
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// fetch the object using the art data manager service
    	// TODO this can throw an exception if the data is not ready in which case we should 1) be prepared to handle that exception and 2) respond with appropriate error
    	Derivative d = artDataManager.fetchDerivativeByImageID(id);
    	if (d == null)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    	
    	// if we have gotten to this point, then we found the art object and can construct a response
    	ImageRecord ir = new ImageRecord(d);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		 
		return new ResponseEntity<RecordContainer>(new RecordContainer(ir), headers, HttpStatus.OK);
	}

    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    @RequestMapping("/media/{source}/images/{id}")
    public ResponseEntity<InputStreamResource> imageContent(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
    	
    	log.debug("SOURCE: " + source);
    	// not implemented if the source is not specified OR (preferably) a redirect to the generic search for image records
    	if (source == null) {
   			log.error("Unspecified source in URL for image content: ");
   			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	}
 
    	// not found if the source != the web images repository (at least for now)
    	if (!source.equals(ArtObjectImage.defaultSource))
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	if (StringUtils.isNullOrEmpty(id))
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// fetch the object using the art data manager service
    	// TODO this can throw an exception if the data is not ready in which case we should 1) be prepared to handle that exception and 2) respond with appropriate error
    	Derivative d = artDataManager.fetchDerivativeByImageID(id);
    	if (d == null)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

		//response.setHeader("content-type",  d.getFormat().getMimetype());

    	URL imageURL = null;
		imageURL = new URL("http:" + d.getSourceImageURI().toString());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.getFormat().getMimetype()))
                .body(new InputStreamResource(imageURL.openStream()));
		
/*		InputStream istream = imageURL.openStream();
    	final byte[] buffer = new byte[1024*1024];
    	while (true) {
    	    final int len = istream.read(buffer);
    	    if (len <= 0)
    	        break;
    	    OutputStream os = response.getOutputStream();
    	    os.write(buffer, 0, len);
    	}
    	
		return new ResponseEntity<RecordContainer>(null, null, HttpStatus.OK);
		*/
		
		
	}

    
}