package gov.nga.integration.cspace;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;

@RestController
public class RunAllTestsController {
	
	@Autowired
	ImageSearchController imageSearchController;
	
	@Autowired
	ArtDataManagerService artDataManager;
	
	private static final Logger log = LoggerFactory.getLogger(RunAllTestsController.class);
    
    @RequestMapping("/runtests")
    public ResponseEntity<RecordContainer> imageRecordSource(
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {

    	log.info("Starting Tests");
    	CSpaceSpringApplicationTest.setArtDataManager(artDataManager);
    	Computer computer = new Computer();
    	JUnitCore jUnitCore = new JUnitCore();
    	Result testResult = jUnitCore.run(computer, CSpaceSpringApplicationTest.class);
    	for (Failure f : testResult.getFailures()) {
    		log.error("FAILED TEST: " + f.toString()); //, f.getException());
    	}
    	int total = testResult.getRunCount();
    	int failed = testResult.getFailureCount();
    	log.info("Finished Tests: " + (total - failed) + " / " + total + " passed.");
    	CSpaceSpringApplicationTest.setArtDataManager(null);
    	if (failed > 0)
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    	else
    		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
   
}