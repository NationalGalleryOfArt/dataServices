/*
    NGA Art Data API: Thumbnail class provides a container for bytes of a small image
    (presumably representing a thumbnail of a larger image) and allows for those bytes
    to be represented either as a base64 encoded stream or a URL to an image which is
    in keeping with the two modes of thumbnails supported by the art data api in search
    results.

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

package gov.nga.imaging;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thumbnail {

	private static final Logger log = LoggerFactory.getLogger(Thumbnail.class);
	
	String representation=null;

	public Thumbnail(URI relativeURI, URL absoluteURL, boolean base64Preferred) {
		this(relativeURI, absoluteURL,"JPEG",base64Preferred);
	}
	
	public Thumbnail(byte[] bytes) {
		// we will always use base64 when dealing with blobs rather than URLs
		this.representation = Base64.encodeBase64String(bytes);
	}
	
	public Thumbnail(URI relativeURI, URL absoluteURL, String encodingFormat, boolean base64Preferred) {
		// returning a protocol relative URI is fine for a web browser but we also need to fetch this image
		// which means we need an absolute URI as well when base64 encoding
		this.representation = relativeURI.toString();
		try {
			if (base64Preferred) {
				BufferedImage img = ImageIO.read(absoluteURL);
				if (img != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ImageIO.write(img, encodingFormat, bos);
					byte[] imageBytes = bos.toByteArray();
					this.representation = Base64.encodeBase64String(imageBytes);
				}
			}
		}
		catch (IOException io) {
			// if we couldn't load the image and encode it, then we cannot honor the base64
			log.error("No thumbnail image data available from URL: " + absoluteURL);
		}
	}

	public String toString() {
		return representation;
	}
	
}
