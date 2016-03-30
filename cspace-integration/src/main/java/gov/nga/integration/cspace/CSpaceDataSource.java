package gov.nga.integration.cspace;

import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author David Beaudet
 *
 */
@Configuration
public class CSpaceDataSource extends DataSourceService {

	private static final Logger log = LoggerFactory.getLogger(CSpaceDataSource.class);
	
	@Autowired
	ConfigService config;	// spring configuration

	@PostConstruct
	private void postConstruct() {
		setDriverClassName(config.getString("jdbc.driverClassName"));
	    setUrl(config.getString("jdbc.url"));
	    setUsername(config.getString("jdbc.username"));
	    setPassword(config.getString("jdbc.password"));
	}
	
	@PreDestroy
	private void preDestroy() {
		log.info("Closing all cached database connections");
		this.closeAll();
	}

}