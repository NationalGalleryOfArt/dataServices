/*
    NGA ART DATA API: ImageThumbnailWorker represents a parallelizable nugget of work that is
    distributed out by the ImageSearchController to fetch thumbnails for all images and / or objects
    within a search result 

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
package gov.nga.integration.cspace;

import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.imaging.Thumbnail;

public class ImageThumbnailWorker implements Callable<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ImageThumbnailWorker.class);
	static {log.debug(ImageThumbnailWorker.class.getName() + " starting up"); }

	private static LinkedHashMap<Object, String> thumbnailCache = new LinkedHashMap<Object, String>();
	
	private static final int MAXDIM=400;
	private static final int MAXSIZE=5000; // cache a maximum number of thumbnails before pruning the list 
	private CSpaceImage image;
	private int width=90;
	private int height=90;
	private boolean useBase64IfPossible;

	public ImageThumbnailWorker(CSpaceImage image, int width, int height, boolean useBase64IfPossible) {
		this.image = image;
		width = width < 0 ? 0 : ( width > MAXDIM ? MAXDIM : width );
		height = height < 0 ? 0 : ( height > MAXDIM ? MAXDIM : height );
		this.width=width;
		this.height=height;
		this.useBase64IfPossible=useBase64IfPossible;
	}

	public static void clearCache() {
		synchronized(thumbnailCache) {
			thumbnailCache.clear();
		}
	}
	
	private static String getFromCache(Object o) {
		if (o == null)
			return null;
		synchronized(thumbnailCache) {
			String s = thumbnailCache.get(o);
			// each time the value is fetched, remove it from the cache and re-add it in order to keep the most
			// frequently used items at the end of the list where they are less likely to be pruned
			if (s != null) {
				thumbnailCache.remove(o);
				thumbnailCache.put(o,s);
			}
			return s;
		}
	}

	private static void saveToCache(Object o, String s) {
		if (o == null)
			return;
		synchronized(thumbnailCache) {
			thumbnailCache.put(o,s);
			// prune the cache to max size, removing the first items from the entry set
			// which are the ones less frequently used
			while (thumbnailCache.size() > MAXSIZE) {
				o = thumbnailCache.entrySet().iterator().next().getKey();
				thumbnailCache.remove(o);
			}
		}
	}

	public String call() {
		if (image == null)
			return null;

		String thumbnailRepresentation = getFromCache(image);
		if (thumbnailRepresentation != null)
			return thumbnailRepresentation;
		
		// otherwise generate the thumbnail for the image
		Thumbnail thumb = image.getThumbnail(width, height, MAXDIM, false, useBase64IfPossible, "https");
		if (thumb == null)
			return null;
		String thumbRepresentation = thumb.toString();
		saveToCache(image,thumbRepresentation);
		
		return thumbRepresentation;
	}

}

