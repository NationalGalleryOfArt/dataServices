package gov.nga.integration.cspace;

import gov.nga.utils.StringUtils;

public class NamespaceUtils {
	
	public final static String nameSpaceDelimeter = ":";

	public static String getNamespace(String fieldName, String defaultNamespace) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(nameSpaceDelimeter);
		if (parts.length > 1)
			return parts[0];
		return defaultNamespace;
	}

	public static String stripNamespace(String fieldName) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(nameSpaceDelimeter);
		if (parts.length > 1)
			return parts[1];
		else
			return parts[0];
	}

	public static String ensureNamespace(String fieldName, String defaultNamespace) {
		String fn = stripNamespace(fieldName);
		if (StringUtils.isNullOrEmpty(fn))
			return null;
		String ns = getNamespace(fieldName,defaultNamespace);
		if (StringUtils.isNullOrEmpty(ns))
			return null;
		return ns + nameSpaceDelimeter + fn;
	}

}
