package gov.nga.integration.cspace;

import java.util.List;
import java.util.concurrent.ExecutionException;

import gov.nga.entities.art.ArtObject;
import gov.nga.search.SearchHelper;

public interface ImageSearchProvider {
	
	public abstract List<CSpaceImage> searchImages(
			SearchHelper<ArtObject> aoSearchHelper, 
			SearchHelper<CSpaceImage> derivativeSearchHelper,	
			List<ArtObject> limitToTheseArtObjects) throws InterruptedException, ExecutionException;
	
	public boolean providesSource(String[] sources);
	public String[] getProvidedSources();
	
	
}
