/*
    NGA ART DATA API: WebImageSearchProvider - provides an implementation for executing searches for WebImage(s)
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gov.nga.integration.cspace.imageproviders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectImage;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.MessageSubscriber;
import gov.nga.entities.art.Derivative.IMGVIEWTYPE;
import gov.nga.entities.art.MessageProvider;
import gov.nga.entities.art.MessageProvider.EVENTTYPES;

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
public class WebImageSearchProvider extends SourceProviderImpl implements MessageSubscriber {
	
	@Autowired
	MessageProvider messageProvider;

	@Autowired
	ArtDataManagerService artDataManager;

	@Autowired
	CSpaceTestModeService ts;
	
	private static final Logger log = LoggerFactory.getLogger(WebImageSearchProvider.class);

	private static final String[] providesSource = {ArtObjectImage.defaultSource};
	
	private List<CSpaceImage> imageCache = null;
	
	public synchronized void receiveMessage(EVENTTYPES event) {
		if (event == EVENTTYPES.DATAREFRESHED) {
			//if (artDataManager != null) {
				// re-cache the image list and object marker
				Collection<ArtObject> newObjectMarker = artDataManager.getArtObjectsRaw().values();
				List<CSpaceImage> newImageCache = getLargestImagesOfArtObjects(newObjectMarker);
				imageCache = newImageCache;
			//}
		}
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
	
	@PreDestroy
	public void preDestroy() {
		log.info("Unregistering from event notifications");
		messageProvider.unsubscribe(this);
	}
	
	public String[] getProvidedSources() {
		return providesSource;
	}

	@PostConstruct
	public void postConstruct() throws Exception {
		messageProvider.subscribe(this);
		// can't receive messages here because the data isn't loaded yet, duh!
	//	receiveMessage(EVENTTYPES.DATAREFRESHED);
	}
	
}
