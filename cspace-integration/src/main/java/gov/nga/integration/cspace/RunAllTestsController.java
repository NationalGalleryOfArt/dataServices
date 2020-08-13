// TODO - turn into JUNIT TESTS in concert with other refactoring of the CSpaceSpringApplicationTest class
/*
    NGA ART DATA API: RunAllTestsController maps to a url that can be used to launch a subset of the 
    integration tests.  For now, it is recommend to limit access to the /runtests URL on production servers
    as the tests can generate a moderate performance load on the system hosting the APIs.
  
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

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.junit.runner.Computer;
//import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;

@RestController
public class RunAllTestsController {
	
	@Autowired
	ImageSearchController imageSearchController;
	
	@Autowired
	ArtDataManagerService artDataManager;
	
	private static final Logger log = LoggerFactory.getLogger(RunAllTestsController.class);
    
    @RequestMapping(value="/runtests",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<String> imageRecordSource(
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {

    	if ( !artDataManager.getConfig().getBoolean(CSpaceConfigService.testingEnabled) ) {
    		String msg = "Received test request, but testing not enabled in this environment";
    		log.warn(msg);
    		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(msg);
    	}
    	
    	log.info("Starting Tests");
    	//CSpaceSpringApplicationTest.setArtDataManager(artDataManager);
    	// execute all tests behind the scenes
    	RunAllPackageTestsSuite.main(null);
    	Result testResult = RunAllPackageTestsSuite.runTests();
    	//Computer computer = new Computer();
    	//JUnitCore jUnitCore = new JUnitCore();
    	//Result testResult = jUnitCore.run(computer, CSpaceSpringApplicationTest.class);
    	for (Failure f : testResult.getFailures()) {
    		log.error("FAILED TEST: " + f.toString()); //, f.getException());
    	}
    	int total = testResult.getRunCount();
    	int failed = testResult.getFailureCount();
    	String testSummary = "Finished Tests: " + (total - failed) + " / " + total + " passed."; 
    	log.info(testSummary);
    	//CSpaceSpringApplicationTest.setArtDataManager(null);
    	if (failed > 0) 
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(testSummary);
    	else
    		return ResponseEntity.status(HttpStatus.OK).body(testSummary); // return a properly formatted error here according to cspace spec
	}
   
}