package gov.nga.integration.cspace;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.*;
import org.junit.runner.RunWith;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.extensions.cpsuite.SuiteType.*;

@RunWith(ClasspathSuite.class)
@SuiteTypes({ TEST_CLASSES })
public class RunAllPackageTestsSuite {
	/* main method not needed, but I use it to run the tests */
	public static void main(String args[]) {
		runTests();
	}
	
	public static Result runTests() {
		Computer computer = new Computer();
    	JUnitCore jUnitCore = new JUnitCore();
		return jUnitCore.run(computer, RunAllPackageTestsSuite.class);
		//return JUnitCore.runClasses(RunAllPackageTestsSuite.class);
	}

}
