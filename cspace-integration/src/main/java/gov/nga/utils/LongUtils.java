package gov.nga.utils;

public class LongUtils {
	
	public static Long stringToLong(String s, Long defaultValue) {
		if (s == null)
			return defaultValue;
		try {
			return Long.valueOf(s);
		}
		catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

}
