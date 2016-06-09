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
