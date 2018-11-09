package gov.nga.integration.cspace;


import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import gov.nga.utils.ConfigService;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)  // default is singleton, but best to be explicit
public class ArtObjectPredicates extends Predicates {
	
	//private static final Logger log = LoggerFactory.getLogger(ImagePredicates.class);

	@Autowired
	ConfigService config;	// access to application.properties file

	private static final String hasParentLabel 	= "nga.api.art.lod.predicates.labels.hasParent";
	private static final String hasChildLabel 	= "nga.api.art.lod.predicates.labels.hasChild";
	private static final String hasSiblingLabel = "nga.api.art.lod.predicates.labels.hasSibling";
	private static final String producedByLabel	= "nga.api.art.lod.predicates.labels.producedBy";
	
	public enum ARTOBJECTPREDICATES implements EnumLabeledInterface {

		HASPARENT,
		HASCHILD,
		HASSIBLING,
		PRODUCEDBY;

		private void setLabels(String[] labels) {
			EnumLabeledInterface.setLabels(testMode, ARTOBJECTPREDICATES.class.getName(), toString(), labels);
		}
		
		public String[] getLabels() {
			return EnumLabeledInterface.getLabels(ARTOBJECTPREDICATES.class.getName(), toString());
		}

		
	};

	@PostConstruct
	private void init() {
		ARTOBJECTPREDICATES.HASPARENT.setLabels(config.getStrings(hasParentLabel,","));
		ARTOBJECTPREDICATES.HASCHILD.setLabels(config.getStrings(hasChildLabel,","));
		ARTOBJECTPREDICATES.HASSIBLING.setLabels(config.getStrings(hasSiblingLabel,","));
		ARTOBJECTPREDICATES.PRODUCEDBY.setLabels(config.getStrings(producedByLabel,","));
	}

	
	// initialize the enum when this service starts up
	public ArtObjectPredicates() {
	}

}