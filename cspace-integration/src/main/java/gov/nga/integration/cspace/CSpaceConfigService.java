package gov.nga.integration.cspace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.nga.utils.ConfigService;

@Configuration
@Component
public class CSpaceConfigService implements ConfigService {
	
	public static final String thumbnailWidthProperty 	= "thumbnailWidth";
	public static final String thumbnailHeightProperty 	= "thumbnailHeight";
	public static final String tmsDBDriverProperty		= "jdbc.driverClassName";
    public static final String tmsDBURLProperty			= "jdbc.url";
    public static final String tmsDBUserNameProperty	= "jdbc.username";
    public static final String tmsDBPasswordProperty	= "jdbc.password";

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


}
