package gov.nga.integration.cspace;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.utils.StringUtils;

@RestController
public class RunAllTestsController {
    
	@Autowired
	ImageSearchController imageSearchController;
	
	@Autowired
	ArtDataManagerService artDataManager;
	
    @Autowired
    private ServletWebServerApplicationContext server;

    public String getHostPort() {
        // String port = System.getProperty("server.port","");
        Integer p = server.getWebServer().getPort();
        String port = p.toString();
        if (!StringUtils.isNullOrEmpty(port))
            port = ":" + port;
        return "http://localhost" + port;
    }

	private static final Logger log = LoggerFactory.getLogger(RunAllTestsController.class);
    
    @RequestMapping("/runtests")
    public ResponseEntity<String> imageRecordSource(
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {

    	log.info("Starting Tests");

    	//CSpaceSpringApplicationTest.setArtDataManager(artDataManager);
    	//Computer computer = new Computer();
    	//JUnitCore jUnitCore = new JUnitCore();
    	//Result testResult = jUnitCore.run(computer, CSpaceSpringApplicationTest.class);
    	//for (Failure f : testResult.getFailures()) {
    	//	log.error("FAILED TEST: " + f.toString()); //, f.getException());
    	//}
    	//int total = testResult.getRunCount();
    	//int failed = testResult.getFailureCount();
    	//log.info("Finished Tests: " + (total - failed) + " / " + total + " passed.");
    	//CSpaceSpringApplicationTest.setArtDataManager(null);
    	try {
    	    CSpaceSpringApplicationTest.runAllTests(artDataManager, getHostPort());
    	}
    	catch ( AssertionError e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage().toString());
    	}
    	return ResponseEntity.status(HttpStatus.OK).body("all tests passed");
	}
   
}