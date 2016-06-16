package gov.nga.integration.cspace.imageproviders;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import gov.nga.entities.art.ArtDataManagerService;

import gov.nga.entities.art.Derivative;
import gov.nga.imaging.Thumbnail;
import gov.nga.integration.cspace.CSpaceImage;

public class WebImage extends CSpaceImage {
	
    private static final Logger log = LoggerFactory.getLogger(WebImage.class);
    static {log.debug(WebImage.class.getName() + " starting up"); }

    public static final String defaultSource = "portfolio-dclpa";
	private static final String CLASSIFICATION = "publishedImage";

	public WebImage(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager, rs);
		setClassifcation(WebImage.CLASSIFICATION);
	}

    public WebImage factory(ResultSet rs) throws SQLException {
        WebImage d = new WebImage(getManager(),rs);
        return d; 
    }
    
    public WebImage(ArtDataManagerService manager) {
    	super(manager);
    	setClassifcation(WebImage.CLASSIFICATION);
    }
    
    public static WebImage factory(Derivative d) {
    	WebImage newImage = new WebImage(d.getManager());
    	BeanUtils.copyProperties(d, newImage);
    	return newImage;
    }

    @Override
	public Thumbnail getThumbnail(int width, int height, int maxdim, boolean exactSizeRequired, boolean preferBase64, String scheme) {
		URI protoRelativeURI = getProtocolRelativeiiifURL(null,"!"+width+","+height,null,null);
		// absolute is the URL used for fetching data for base64 encoding
		URL absoluteURL = null;
		try {
			absoluteURL = new URL(scheme.toLowerCase() + ":" + protoRelativeURI.toString());
		}
		catch (MalformedURLException me) {
			log.error("Problem creating absolute URI for thumbnail", me);
		}
		return new Thumbnail(protoRelativeURI, absoluteURL, preferBase64);
	}
	

	
}
