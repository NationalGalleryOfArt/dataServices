/*
    NGA ART DATA API: ArtObjectThumbnailWorker should no longer be used and will be deleted in a future release
  
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
package gov.nga.integration.cspace.deprecated;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Derivative;
import gov.nga.common.entities.art.Derivative.ImgSearchOpts;
import gov.nga.imaging.Thumbnail;

@Deprecated
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

