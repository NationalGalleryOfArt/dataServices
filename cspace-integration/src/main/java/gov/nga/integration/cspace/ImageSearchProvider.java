package gov.nga.integration.cspace;

import java.util.List;

import gov.nga.entities.art.ArtObject;
import gov.nga.search.SearchHelper;

public interface ImageSearchProvider {
	
	public abstract List<CSpaceImage> searchImages(
			SearchHelper<CSpaceImage> derivativeSearchHelper,	
			List<ArtObject> limitToTheseArtObjects) throws Exception;
	
	public boolean providesSource(String[] sources);
	public String[] getProvidedSources();
	
	
}
