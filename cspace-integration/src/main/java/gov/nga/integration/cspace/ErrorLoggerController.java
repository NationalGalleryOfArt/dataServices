package gov.nga.integration.cspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


// all authorization will be handled by the Apache Server configured as a proxy server for this service
// alternatively, as time permits, we might build authentication into this application via Spring
// authentication entry point configurations and such, but for now we will try to keep it as simple as possible
@RestController
public class ErrorLoggerController {

	private static final Logger log = LoggerFactory.getLogger(ErrorLoggerController.class);

	/*  SPRING REST CONTROLLER NOTES
	 @RequestMapping(value = "/matches/{matchId}", produces = "application/json")
	 	this annotation can be used to match a particular variable from the path in the response handler - pretty cool! 
	 	public String match(@PathVariable String matchId) {
	
	 
	
	 errors can be logged using the old style approach
	 	change your return type to ResponseEntity<String>, then you can use below for 400

		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		and for correct request
		return new ResponseEntity<String>(json,HttpStatus.OK);
		
	 or the new style approach with > 4.1
	 	after spring 4.1 there are helper methods in ResponseEntity could be used as

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		and
		return ResponseEntity.ok(json);
	
	 Mappings between request params and variables in a RequestMapping method can be defined as follows
	 	@RequestParam(value="severity", defaultValue="error") String myVar
	
	 @ResponseBody 
	 	this annotation can be used to respond directly with the JSON response to avoid having to create
	 	a whole response class structure to encode the response - might use this approach for the non-error services
	 	
	 @RequestMapping(method=RequestMethod.GET, value="/fooBar")
		public ResponseEntity<String> fooBar2() {
    		String json = "jsonResponse";
    		HttpHeaders responseHeaders = new HttpHeaders();
    		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    		return new ResponseEntity<String>(json, responseHeaders, HttpStatus.CREATED);
		}
		
		
	 // you can get the request and response by simply adding them to method signature 
	 @RequestMapping(value = "/report/{objectId}", method = RequestMethod.GET)
	 public @ResponseBody void generateReport(
     	@PathVariable("objectId") Long objectId, 
        HttpServletRequest request, 
        HttpServletResponse response) {
	 	
	 */

	@RequestMapping("/system/logger.json")
	public ResponseEntity<ErrorLoggerResponse> logger(
			@RequestParam(value="severity", required=true) String severity,
			@RequestParam(value="origin", 	required=true) String origin,
			@RequestParam(value="summary", 	required=true) String summary,
			@RequestParam(value="details", 	required=true) String details
	) {
		// validate the request and response accordingly if there are problems
		if (severity == null || origin == null || summary == null || details == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		
		String message = summary + ": " + details + " (" + origin + ")";
		
		switch (severity.toLowerCase()) {
			case "fatal" : log.error("FATAL ERROR:"+message); break;
			case "error" : log.error(message); break;
			case "warn"  : log.warn(message);  break;
			case "info"  : log.info(message);  break;
			case "debug" : log.debug(message); break;
			case "trace" : log.trace(message); break;
			default 	 : return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		// assemble a well formed response
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ErrorLoggerResponse er = new ErrorLoggerResponse(severity, origin, summary, details); 
		return new ResponseEntity<ErrorLoggerResponse>(er, headers, HttpStatus.OK);
	}

}

