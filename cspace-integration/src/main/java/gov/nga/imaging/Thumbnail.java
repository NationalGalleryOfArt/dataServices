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
			log.error("Problem encountered creating thumbnail, so no thumbnail", io);
		}
	}

	public String toString() {
		return representation;
	}
	
}
