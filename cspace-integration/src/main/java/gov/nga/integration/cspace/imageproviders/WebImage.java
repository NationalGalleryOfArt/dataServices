/*
    NGA ART DATA API: WebImage represents an art object image that has been made web-ready 
  
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
import gov.nga.integration.cspace.CSpaceTestModeService;

public class WebImage extends CSpaceImage {
	
    private static final Logger log = LoggerFactory.getLogger(WebImage.class);
    static {log.debug(WebImage.class.getName() + " starting up"); }

	private static final String CLASSIFICATION = "publishedImage";
	
	public WebImage(ArtDataManagerService manager, ResultSet rs, CSpaceTestModeService ts) throws SQLException {
		super(manager, rs);
		if ( ts.isTestModeOtherHalfObjects() )
			setClassification("partner2" + WebImage.CLASSIFICATION);
		else
			setClassification(WebImage.CLASSIFICATION);
	}

//    public WebImage factory(ResultSet rs, CSpaceTestModeService ts) throws SQLException {
//        WebImage d = new WebImage(getManager(),rs, ts);
//        return d; 
//    }
    
    public WebImage(ArtDataManagerService manager, CSpaceTestModeService ts) {
    	super(manager);
		if ( ts.isTestModeOtherHalfObjects() )
			setClassification("partner2" + WebImage.CLASSIFICATION);
		else
			setClassification(WebImage.CLASSIFICATION);
    }
    
    public static WebImage factory(Derivative d, CSpaceTestModeService ts) {
    	WebImage newImage = new WebImage(d.getManager(), ts);
    	BeanUtils.copyProperties(d, newImage);
    	return newImage;
    }

    @Override
	public Thumbnail getThumbnail(int width, int height, int maxdim, boolean exactSizeRequired, boolean preferBase64, String scheme) {
    	// first, we try to get a IIIF URL so we can get the right size thumbnail
		URI protoRelativeURI = getProtocolRelativeiiifURL(null,"!"+width+","+height,null,null);
		
		// no luck at getting a IIIF URL
		if (protoRelativeURI == null) {
			
			// if caller needs the exact size and we don't have the exact size then they don't get a thumbnail
			if (exactSizeRequired && ( getWidth() != width || getHeight() != height) )
				return null;
			// if we are too large to serve as a good thumbnail, then don't return a thumbnail
			if (getWidth() > maxdim || getHeight() > maxdim)
				return null;
			
			// otherwise, we'll use what we've got
			protoRelativeURI = getSourceImageURI();
		}
		
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
