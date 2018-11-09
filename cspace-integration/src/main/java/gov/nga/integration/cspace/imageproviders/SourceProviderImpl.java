package gov.nga.integration.cspace.imageproviders;

import gov.nga.integration.cspace.ImageSearchProvider;
import gov.nga.integration.cspace.SourceProvider;

public abstract class SourceProviderImpl implements SourceProvider, ImageSearchProvider {
	
	public abstract String[] getProvidedSources();
	
	public boolean providesSource(String[] source) {
		for (String s : source)
			for (String p : getProvidedSources())
				if (s.equals(p))
					return true;
		return false;
	}
	
}
