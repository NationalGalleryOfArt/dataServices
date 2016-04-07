package gov.nga.integration.cspace;

public class NamespaceUtils {

	public static String getNamespace(String fieldName, String defaultNamespace) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(":");
		if (parts.length > 1)
			return parts[0];
		return defaultNamespace;
	}

	public static String stripNamespace(String fieldName) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(":");
		if (parts.length > 1)
			return parts[1];
		else
			return parts[0];
	}

}
