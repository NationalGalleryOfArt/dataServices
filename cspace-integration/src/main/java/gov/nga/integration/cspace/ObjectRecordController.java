package gov.nga.integration.cspace;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.utils.LongUtils;

@RestController
public class ObjectRecordController {

	private static final Logger log = LoggerFactory.getLogger(ObjectRecordController.class);
    
    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping("/art/objects/{id}.json")
    public ResponseEntity<RecordContainer> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) {
    	return objectRecordSource(null, id, request, response);
    }

    @RequestMapping("/art/{source}/objects/{id}.json")
    public ResponseEntity<RecordContainer> objectRecordSource(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) {
    	
    	log.info("SOURCE: " + source);
    	// not implemented if the source is not specified OR (preferably) a redirect to the generic search for object records
    	if (source == null) {
    		try {
    			return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(new URI(response.encodeRedirectURL("/art/objects?id="+id))).body(null);
    		}
    		catch (Exception ue) {
    			log.error("Unexpected exception with URI: " + ue.getMessage());
    			return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    		}
    	}
    	

    	// not found if the source != tms (at least for now)
    	if (!source.equals("tms"))
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	// validate that the ID conforms to NGA specifications - it must be parseable into a Long for it to be TMS compatible
    	Long l = LongUtils.stringToLong(id, null);
    	if (l == null)
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// fetch the object using the art data manager service
    	// TODO this can throw an exception if the data is not ready in which case we should 1) be prepared to handle that exception and 2) respond with appropriate error
    	ArtObject o = artDataManager.fetchByObjectID(l);
    	if (o == null)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    	
    	// if we have gotten to this point, then we found the art object and can construct a response
    	ObjectRecord or = new ObjectRecord(o,artDataManager.getLocationsRaw());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		 
		return new ResponseEntity<RecordContainer>(new RecordContainer(or), headers, HttpStatus.OK);
	}

    
}