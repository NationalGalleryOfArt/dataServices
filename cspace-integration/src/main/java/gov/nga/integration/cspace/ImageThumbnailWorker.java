package gov.nga.integration.cspace;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

import gov.nga.entities.art.Derivative;

public class ImageThumbnailWorker implements Callable<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ImageThumbnailWorker.class);

	private static Map<Object, String> thumbnailCache = new ConcurrentHashMap<Object, String>();
	
	private static final int MAXDIM=400;
	private Derivative image;
	private int width=90;
	private int height=90;
	private boolean useBase64IfPossible;

	public ImageThumbnailWorker(Derivative image, int width, int height, boolean useBase64IfPossible) {
		this.image = image;
		width = width < 0 ? 0 : ( width > MAXDIM ? MAXDIM : width );
		height = height < 0 ? 0 : ( height > MAXDIM ? MAXDIM : height );
		this.width=width;
		this.height=height;
		this.useBase64IfPossible=useBase64IfPossible;
	}

	synchronized public static void clearCache() {
		thumbnailCache = new ConcurrentHashMap<Object, String>();
	}

	synchronized public static Map<Object, String> getCache() {
		return thumbnailCache;
	}

	public String call() {
		
		boolean base64=useBase64IfPossible;
		
		// use IIIF to serve the exact size we need for the thumbnail images - this means prod will need IIIF which is fine
		// since we're moving that way anyway
		//Derivative d = object.getLargeThumbnail(ImgSearchOpts.FALLBACKTOLARGESTFIT);
		String thumbnail = null;
		if (image != null) {

			if ( base64 && thumbnailCache.containsKey(image) )
				return thumbnailCache.get(image);

			// form the proper IIIF URL to resize to a bounding box
			URI protoRelativeURI = image.getProtocolRelativeiiifURL(null,"!"+width+","+height,null,null);
			if (protoRelativeURI == null) {
				// no ptif available, so we use a fallback of browser download provided image is small enough
				// then we don't have a valid PTIF source for the image so we can just send the whole image as a URL 
				// rather than base 64 encoded, provided it's small enough - consider 100,000 pixels (300x300) as small enough to be downloaded by browser
				if (image.getWidth() * image.getHeight() > MAXDIM*MAXDIM)
					return null;
				protoRelativeURI = image.getSourceImageURI();
				base64 = false;
			}
			// absolute is the URL used for fetching data for base64 encoding
			URL absoluteURL = null;
			try {
				absoluteURL = new URL("http:" + protoRelativeURI.toString());
			}
			catch (MalformedURLException me) {
				log.error("Problem creating absolute URI for thumbnail:" + me.getMessage());
			}
			Thumbnail thumb = new Thumbnail(protoRelativeURI, absoluteURL, base64);
			thumbnail = thumb.toString();
			if (base64)
				thumbnailCache.put(image, thumbnail);
		}
		return thumbnail;
	}
}

