package gov.nga.integration.cspace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.nga.utils.ConfigService;

@Configuration
@Service
public class CSpaceConfigService implements ConfigService, CSpaceTestModeService {
	
	public static final String thumbnailWidthProperty 	= "thumbnailWidth";
	public static final String thumbnailHeightProperty 	= "thumbnailHeight";
	
	public static final String multiTenancyTestMode 				= "testMode";
	public static final String multiTenancyTestModeHalfObjects 		= "halfObjects";
	public static final String multiTenancyTestModeOtherHalfObjects = "otherHalfObjectsWithOntologyChanges";
	public static final String unloadBeforeLoading 					= "dumpFromMemoryBeforeLoading";
	
	@Autowired
	private Environment env;
	
	// prepend propertyName with "ngaweb" and return that by default if it exists
	public String getString(String propertyName) {
		if (env.getProperty("ngaweb."+propertyName) == null)
			return env.getProperty(propertyName);
		
		return env.getProperty("ngaweb."+propertyName);
	}
	
	public Integer getInteger(String propertyName) {
		if (env.getProperty("ngaweb."+propertyName) == null)
			return env.getProperty(propertyName, Integer.class);
		
		return env.getProperty("ngaweb."+propertyName, Integer.class);
	}
	
	public boolean unloadBeforeLoading() {
		String unloadFirst = getString(unloadBeforeLoading);
		return unloadFirst == null || unloadFirst.equals("true");
	}
	
	public boolean isTestModeHalfObjects() {
		String testmode = getString(multiTenancyTestMode);
		return testmode != null && testmode.equals(multiTenancyTestModeHalfObjects);
	}
	
	public boolean isTestModeOtherHalfObjects() {
		String testmode = getString(multiTenancyTestMode);
		return testmode != null && testmode.equals(multiTenancyTestModeOtherHalfObjects);
	}

}
