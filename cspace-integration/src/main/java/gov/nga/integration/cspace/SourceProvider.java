package gov.nga.integration.cspace;

public interface SourceProvider {
	public boolean providesSource(String[] sources);
	public String[] getProvidedSources();
}
