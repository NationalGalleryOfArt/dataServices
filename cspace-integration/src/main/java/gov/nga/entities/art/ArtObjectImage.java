

package gov.nga.entities.art;

import gov.nga.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;


//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ArtObjectImage extends Derivative {

    // private static final Logger log = LoggerFactory.getLogger(ArtObjectImage.class);

    protected static final String PRIMARY_OBJECT_IMAGE_SEQUENCE = Long.toString(0);
    private static final Logger log = LoggerFactory.getLogger(ArtObjectImage.class);

    public ArtObjectImage(ArtDataManagerService manager) {
        super(manager);
    }

    private static final String fetchAllImagesQuery = 
        "SELECT imageID,  imgVolumePath, filename,     format, " + 
        "       width,    height,        targetWidth,  targetHeight, " +
        "       viewType, sequence,      tmsObjectID " +
        "FROM data.object_images ";

    protected String getAllImagesQuery() {
        return fetchAllImagesQuery;
    }
    
    private ArtObject artObject = null;
    public ArtObject getArtObject() {
        if (artObject == null)
            try {
                artObject = getManager().fetchByObjectID(getObjectID());
            } catch (DataNotReadyException exception) {
                log.error("Can not fetch By Object ID: " + exception.getMessage());
            }
        return artObject;
    }

    public ArtObjectImage(
            ArtDataManagerService manager, 
            String imageID, String imgVolumePath, String filename, String ft, 
            Long width, Long height, Long targetWidth, Long targetHeight,
            String vt, String sequence, Long tmsObjectID ) throws SQLException  {
        
        this(manager);
        setImageID(imageID);
        setImgVolumePath(imgVolumePath);
        setFilename(filename);
        setWidth(width);
        setHeight(height);
        setTargetWidth(targetWidth);
        setTargetHeight(targetHeight);
        setSequence(sequence);
        setTmsObjectID(tmsObjectID);

        if (vt.equals(IMGVIEWTYPE.PRIMARY.getLabel()))
            setViewType(IMGVIEWTYPE.PRIMARY);
        else if (vt.equals(IMGVIEWTYPE.CROPPED.getLabel()))
            setViewType(IMGVIEWTYPE.CROPPED);
        else 
            setViewType(IMGVIEWTYPE.ALTERNATE);
        
        if (ft.equals(IMGFORMAT.PTIF.getLabel()))
            setFormat(IMGFORMAT.PTIF);
        else 
            setFormat(IMGFORMAT.JPEG);
    }

    public ArtObjectImage(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
        this(
                manager, 
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                TypeUtils.getLong(rs, 5), TypeUtils.getLong(rs, 6), TypeUtils.getLong(rs, 7), TypeUtils.getLong(rs, 8), 
                rs.getString(9), rs.getString(10), TypeUtils.getLong(rs, 11)
        );
    }

    public Derivative factory(ResultSet rs) throws SQLException {
        ArtObjectImage d = new ArtObjectImage(getManager(),rs);
        return d; 
    }
    
    public IMAGECLASS getImageClass() {
        return IMAGECLASS.ARTOBJECTIMAGE;
    }
    
    public static boolean isPrimaryView(Derivative d) {
    	return ( d.getViewType() == IMGVIEWTYPE.PRIMARY && d.getSequence().equals(PRIMARY_OBJECT_IMAGE_SEQUENCE)) ;
    }
    
}

