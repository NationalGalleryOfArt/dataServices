package gov.nga.integration.cspace;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObjectImage;

import gov.nga.imaging.Thumbnail;

public abstract class CSpaceImage extends ArtObjectImage {

    public CSpaceImage(ArtDataManagerService manager) {
        super(manager);
    }
    
    public CSpaceImage(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
        super(manager, rs); 
    }


    // return a thumbnail as close as possible to the desired size (or exact size only if an exact size is required
    // send back a base64 encoded stream of bytes if base64 is preferred and feasible to do so 
    // or return a URL to a thumbnail if one is already created
    // otherwise return NULL  
    public abstract Thumbnail getThumbnail(int width, int height, int maxdim, boolean exactSizeRequired, boolean preferBase64, String scheme);

}
