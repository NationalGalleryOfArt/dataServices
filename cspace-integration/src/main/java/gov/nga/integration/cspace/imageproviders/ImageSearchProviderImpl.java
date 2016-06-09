package gov.nga.integration.cspace.imageproviders;

import gov.nga.integration.cspace.ImageSearchProvider;

public abstract class ImageSearchProviderImpl implements ImageSearchProvider {
	
	public abstract String[] getProvidedSources();
	
	public boolean providesSource(String[] source) {
		for (String s : source)
			for (String p : getProvidedSources())
				if (s.equals(p))
					return true;
		return false;
	}
	
}
