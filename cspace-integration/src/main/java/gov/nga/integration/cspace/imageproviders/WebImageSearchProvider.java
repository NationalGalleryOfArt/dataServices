package gov.nga.integration.cspace.imageproviders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nga.entities.art.ArtDataManager;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGVIEWTYPE;
import gov.nga.integration.cspace.CSpaceImage;
import gov.nga.integration.cspace.CSpaceTestModeService;
import gov.nga.search.ResultsPaginator;
import gov.nga.search.SearchHelper;
import gov.nga.search.SortHelper;
import gov.nga.utils.CollectionUtils;

// we have to register this with Spring in order to use Spring's bean services to get access to it later as an implementer
// in this case, it doesn't really matter technically whether it's a generic component, a bean, a service, etc. but 
// since it most closely resembles a service, we'll use that component type 
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)  // default is singleton, but best to be explicit
public class WebImageSearchProvider extends ImageSearchProviderImpl {
	
	@Autowired
	ArtDataManager artDataManager;
	
	@Autowired
	CSpaceTestModeService ts;

	private static final String[] providesSource = {ArtObjectImage.defaultSource};
	
	private List<CSpaceImage> imageCache = null;
	private Collection<ArtObject> objectMarker = null;
	
	public void clearImageCache() {
		imageCache = null;
	}

	private List<CSpaceImage> getLargestImagesOfArtObjects(Collection<ArtObject> fromTheseObjects) {
		List<CSpaceImage> images = CollectionUtils.newArrayList();
		// add the largest images of each type to the results
		// loop through the list of images once, creating our initial list of Derivatives
		for (ArtObject ao : fromTheseObjects) {
			// add all images from this object to the list of Derivatives
			List<Derivative> largest = ao.getLargestImages(IMGVIEWTYPE.allViewTypesExcept(IMGVIEWTYPE.CROPPED)); 
			for (Derivative d : largest) {
				if (d != null) {
					WebImage w = WebImage.factory(d, ts); 	// create a copy of the Derivative as a WebImage instead
					images.add(w);
				}
			}
		}
		return images;
	}

	public List<CSpaceImage> searchImages(
//			SearchHelper<ArtObject> aoSearchHelper, 
			SearchHelper<CSpaceImage> imageSearchHelper,
			List<ArtObject> limitToTheseArtObjects) throws InterruptedException, ExecutionException {

		List<CSpaceImage> images = CollectionUtils.newArrayList();

		// if we're not limiting to particular art objects, we still need to
		// consider ALL art objects when searching for web image repository images since that's the
		// way we get to the images in the first place
		if (limitToTheseArtObjects != null)
			images = getLargestImagesOfArtObjects(limitToTheseArtObjects);
		else {
			artDataManager.isDataReady(true);
			// if we haven't yet cached the list of images or the list has changed since we cached it, then refresh it
			if ( objectMarker == null || !objectMarker.equals(artDataManager.getArtObjectsRaw().values()) ) {
				objectMarker = artDataManager.getArtObjectsRaw().values();
				imageCache = getLargestImagesOfArtObjects(objectMarker);
			}
			images = imageCache;
		}

		// execute the search across these derivatives for any derivative specific fields - other implementers will have to
    	// do the same thing - sorting will take place afterwards
    	return imageSearchHelper.search(images, (ResultsPaginator) null, null, (SortHelper<Derivative>) null);
	}
	
	// default modifier to prevent subclasses and other packages from acquiring an instance
	WebImageSearchProvider() {
		super();
	}
	
	public String[] getProvidedSources() {
		return providesSource;
	}
	
}
