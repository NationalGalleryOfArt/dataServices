package gov.nga.integration.cspace;

import javax.servlet.http.HttpServletRequest;

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

@RestController
public class ObjectRecordController {

	//private static final Logger log = LoggerFactory.getLogger(ObjectRecordController.class);
    
    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping("/art/{source}/objects/{id}.json")
    public ResponseEntity<ObjectRecord> objectRecord(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request
	) {
    	
    	// bad request if the source is not specified
    	if (source == null)
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

    	// not found if the source != tms (at least for now)
    	if (!source.equals("tms"))
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	// validate that the ID conforms to NGA specifications - it must be parseable into a Long for it to be TMS compatible
    	Long l = null;
    	try {
    		l = Long.decode(id); 
    	}
    	catch (NullPointerException np) {}
    	catch (NumberFormatException nf) {}
    	if (l == null)
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// fetch the object using the art data manager service
    	ArtObject o = artDataManager.fetchByObjectID(l);
    	if (o == null)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    	
    	// if we have gotten to this point, then we found the art object and can construct a response
    	ObjectRecord or = new ObjectRecord(o,artDataManager.getLocationsRaw());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		 
		return new ResponseEntity<ObjectRecord>(or, headers, HttpStatus.OK);
	}

    
}