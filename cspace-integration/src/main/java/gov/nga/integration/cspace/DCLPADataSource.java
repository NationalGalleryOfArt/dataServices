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
@Configuration(value="nga.jdbc.dclpa")
public class DCLPADataSource extends DataSourceService {

	private static final Logger log = LoggerFactory.getLogger(DCLPADataSource.class);
	
	private static final String dbURL 	= "nga.jdbc.dclpa.url";
	private static final String dbUser 	= "nga.jdbc.dclpa.username";
	private static final String dbPass	= "nga.jdbc.dclpa.password";
	private static final String dbValid = "nga.jdbc.dclpa.validationQuery";
	
	@Autowired
	ConfigService config;	// access to application.properties file

	@PostConstruct
	private void postConstruct() {
		init(
				config.getString(dbURL),
				config.getString(dbUser),
				config.getString(dbPass),
				config.getString(dbValid)
		);
	}
	
	@PreDestroy
	private void preDestroy() {
		log.info("Nothing to do here since pool implementation is responsible for closing any open connections.");
	}

}