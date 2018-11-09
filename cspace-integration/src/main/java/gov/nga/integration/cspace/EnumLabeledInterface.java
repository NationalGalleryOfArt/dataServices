package gov.nga.integration.cspace;

import java.util.Map;

import gov.nga.utils.CollectionUtils;

public interface EnumLabeledInterface {
	
	// using a map, we can change the contents of the map and use
	// it in a static context to store all of our enum values, indexed by the name
	// of the class and the value of the enum
	static Map<String,String[]> labels = CollectionUtils.newHashMap();

	static String makeKey(String className, String enumValue) {
		return className + "." + enumValue;
	}
	
	static void setLabels(Boolean testMode, String className, String enumValue, String[] labels) {
		// modify the label if we're running in test mode so that multitenancy can be tested
		// in consuming applications requiring support for multitenancy, e.g. conservation space
		// this isn't critical for the NGA except when testing multitenancy with conservation space
		if ( testMode ) {
			for (int j=0; j<labels.length; j++)
				labels[j] = "partner2" + labels;
		}
		EnumLabeledInterface.labels.put(makeKey(className, enumValue), labels);
	}
	
	static String[] getLabels(String className, String enumValue) {
		
		return labels.get(makeKey(className, enumValue));
	}

}