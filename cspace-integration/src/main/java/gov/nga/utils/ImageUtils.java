package gov.nga.utils;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {

	private static final Logger logger = LoggerFactory
	.getLogger(ImageUtils.class); 
	
	public static String getFileSuffix(final String path) {
	    String result = null;
	    if (path != null) {
	        result = "";
	        if (path.lastIndexOf('.') != -1) {
	            result = path.substring(path.lastIndexOf('.'));
	            if (result.startsWith(".")) {
	                result = result.substring(1);
	            }
	        }
	    }
	    return result;
	}
	public static Dimension getImageDim(String path) {
	    Dimension result = null;
	   
	    String suffix =  "jpg";//getFileSuffix(path);
	    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
	    if (iter.hasNext()) {
	        ImageReader reader = iter.next();
	        try {
	            ImageInputStream stream = new FileImageInputStream(new File(path));
	            reader.setInput(stream);
	            int width = reader.getWidth(reader.getMinIndex());
	            int height = reader.getHeight(reader.getMinIndex());
	            logger.info ("%%%: " + width + ": " + height);
	            result = new Dimension(width, height);
	        } catch (IOException e) {
	        	 
	            logger.error(e.getMessage());
	        } finally {
	            reader.dispose();
	        }
	    } else {
	         logger.error("getImageDim");
	    }
	    return result;
	}
}
