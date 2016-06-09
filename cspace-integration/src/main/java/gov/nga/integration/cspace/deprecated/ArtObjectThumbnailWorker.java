package gov.nga.integration.cspace.deprecated;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.ImgSearchOpts;
import gov.nga.imaging.Thumbnail;

public class ArtObjectThumbnailWorker implements Callable<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ArtObjectThumbnailWorker.class);

	private static Map<Long, String> thumbnailCache = new ConcurrentHashMap<Long, String>();
	
	private ArtObject object;
	
	public ArtObjectThumbnailWorker(ArtObject object) {
		//log.info("--------- worker created for " + object.getObjectID());
		this.object = object;
	}
	
	synchronized public static void clearCache() {
		thumbnailCache = new ConcurrentHashMap<Long, String>();
	}

	synchronized public static Map<Long, String> getCache() {
		return thumbnailCache;
	}

	public String call() {
		
		boolean base64 = true;
		if ( base64 && thumbnailCache.containsKey(object.getObjectID()) ) {
			return thumbnailCache.get(object.getObjectID());
		}
		// TODO - use IIIF to serve the exact size we need for the thumbnail images
		Derivative d = object.getLargeThumbnail(ImgSearchOpts.FALLBACKTOLARGESTFIT);
		String thumbnail = null;
		if (d != null) {
			URI relativeURI = d.getSourceImageURI();
			URL absoluteURL = null;
			// thrown if URI is not absolute
			try {
				absoluteURL = new URL("http:" + relativeURI.toString());
			}
			catch (MalformedURLException me) {
				log.error("Problem creating absolute URI for thumbnail:" + me.getMessage());
			}
			Thumbnail thumb = new Thumbnail(relativeURI, absoluteURL, base64);
			thumbnail = thumb.toString();
			if (base64)
				thumbnailCache.put(object.getObjectID(), thumbnail);
		}
		return thumbnail;
	}
}

