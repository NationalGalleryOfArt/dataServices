package gov.nga.integration.cspace;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;

@RestController
public class ObjectSearchController {

	private static final Logger log = LoggerFactory.getLogger(ObjectSearchController.class);
    
//    @Autowired
//    private ArtDataManagerService artDataManager;

    
    /*
 		@RequestMapping(value = "/report/{objectId}", method = RequestMethod.GET)
		public @ResponseBody void generateReport(
        	@PathVariable("objectId") Long objectId, 
        	HttpServletRequest request, 
        	HttpServletResponse response) {
	
	*/

    @RequestMapping("/art/{source}/objects.json")
    public ResponseEntity<ObjectRecord> objectRecord(
			
    		@RequestParam(value="id", 			required=false) List<String> ids,
			@RequestParam(value="cultObj:id",	required=false) List<String> cultObj_ids,
			
			@RequestParam(value="lastModified", required=false) String lastModified,
			HttpServletRequest request
	) {
    	
    	// combine the ID fields first so that we have only one list
    	
    	if (ids != null && cultObj_ids != null)
    		ids.addAll(cultObj_ids);
    	else if (ids == null)
    		ids = cultObj_ids;
    	
    	
		// validate the request and response accordingly if there are problems
		// assemble a well formed response
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ObjectRecord or = new ObjectRecord(null,null); 
		return new ResponseEntity<ObjectRecord>(or, headers, HttpStatus.OK);
	}

    
}