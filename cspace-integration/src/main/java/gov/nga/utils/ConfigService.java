package gov.nga.utils;

public interface ConfigService {
	public String getString(String PropertyName);
	public String[] getStrings(String PropertyName, String splitOn);
	public Integer getInteger(String ProperyName);
}
