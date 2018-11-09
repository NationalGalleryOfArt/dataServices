package gov.nga.integration.cspace;


import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import gov.nga.utils.ConfigService;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)  // default is singleton, but best to be explicit
public class Predicates {
	
	//private static final Logger log = LoggerFactory.getLogger(ImagePredicates.class);

	@Autowired
	ConfigService config;	// access to application.properties file

	@Autowired
	CSpaceTestModeService ts;
	
	protected static boolean testMode = false;

	@PostConstruct
	private void init() {
		testMode = ts.isTestModeOtherHalfObjects();
	}
	
	public Predicates() {
	}
	
}