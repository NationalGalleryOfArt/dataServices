package gov.nga.integration.cspace;

import org.junit.extensions.cpsuite.ClasspathSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import gov.nga.api.iiif.auth.IIIFImageAPIHandlerIntegrationTest;

//import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

@RunWith(ClasspathSuite.class)
@SuiteClasses( { CSpaceSpringApplicationTest.class, IIIFImageAPIHandlerIntegrationTest.class } )
public class RunAllPackageTestsSuite {
	/* main method not needed, but I use it to run the tests */
	//public static void main(String args[]) {
	//	runTests();
	//}
	
	public static Result runTests() {
		//Computer computer = new Computer();
    	//JUnitCore jUnitCore = new JUnitCore();
		//return jUnitCore.run(computer, RunAllPackageTestsSuite.class);
		return JUnitCore.runClasses( CSpaceSpringApplicationTest.class, IIIFImageAPIHandlerIntegrationTest.class );
	}

}
