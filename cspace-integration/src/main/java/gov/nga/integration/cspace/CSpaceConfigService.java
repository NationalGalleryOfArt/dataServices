/*
    NGA ART DATA API: CSpaceConfigService provides a binding between the application.properties file and
    application behavior - it is the DAO for the application's configuration.

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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.nga.utils.ConfigService;

@Service
public class CSpaceConfigService implements ConfigService, CSpaceTestModeService {
	
	public static final String thumbnailWidthProperty 	= "thumbnailWidth";
	public static final String thumbnailHeightProperty 	= "thumbnailHeight";
	
	public static final String multiTenancyTestMode 				= "testMode";
	public static final String multiTenancyTestModeHalfObjects 		= "halfObjects";
	public static final String multiTenancyTestModeOtherHalfObjects = "otherHalfObjectsWithOntologyChanges";
	public static final String unloadBeforeLoading 					= "dumpFromMemoryBeforeLoading";
	
	public static final String testingEnabled	= "CSpaceTestsEnabled";
	
	//private static final Logger log = LoggerFactory.getLogger(CSpaceConfigService.class);
	
	@Autowired
	private Environment env;
	
	// prepend propertyName with "ngaweb" and return that by default if it exists
	public String getString(String propertyName) {
		if (env.getProperty("ngaweb."+propertyName) == null)
			return env.getProperty(propertyName);
		
		return env.getProperty("ngaweb."+propertyName);
	}

	public String[] getStrings(String propertyName, String splitOn) {
		String propVal = null;
		if (env.getProperty("ngaweb."+propertyName) == null)
			propVal = env.getProperty(propertyName);
		else
			propVal = env.getProperty("ngaweb."+propertyName);
		if (propVal != null)
			return propVal.split(splitOn);
		return null;
	}

	public Integer getInteger(String propertyName) {
		if (env.getProperty("ngaweb."+propertyName) == null)
			return env.getProperty(propertyName, Integer.class);
		
		return env.getProperty("ngaweb."+propertyName, Integer.class);
	}

	public boolean getBoolean(String propertyName, boolean defaultValue) {
		if (env.getProperty("ngaweb."+propertyName) != null)
			return env.getProperty("ngaweb."+propertyName, Boolean.class).booleanValue();
		if (env.getProperty(propertyName) != null)
			return env.getProperty(propertyName, Boolean.class).booleanValue();
		else
			return defaultValue;
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
