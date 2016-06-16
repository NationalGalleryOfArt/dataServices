package gov.nga.integration.cspace.imageproviders;

import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.entities.art.ArtDataManagerService;

import gov.nga.imaging.Thumbnail;
import gov.nga.integration.cspace.CSpaceImage;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.db.DataSourceService;

public class DCLPAImage extends CSpaceImage {
	
    private static final Logger log = LoggerFactory.getLogger(DCLPAImage.class);

    public static final String defaultSource = "portfolio-dclpa";
	private static final Pattern pathTransformationPattern = Pattern.compile(".*/DAR(/.*)");
    public IMAGECLASS getImageClass() {
        return IMAGECLASS.CONSERVATIONIMAGE;
    }

    private DataSourceService dclpaDS;
    
    private static final String fetchAllImagesQuery = "{call cspaceSearch(?,?,?,?,?,?,?)}";
    	// this stored procedure produces record_id, filename, replace(path,':','/') as path, last_modified, width, height, extension_win, objectID,
		// creator, originalSource, originalSourceType, viewDescription, description, projectDescription, lightQuality, spectrum,
    	// treatmentPhase, originalFilename, captureDevice

	private static final String CLASSIFICATION = "conservationImage";
    
    public String getAllImagesQuery() {
    	return fetchAllImagesQuery;
    }

	public DCLPAImage(ArtDataManagerService manager, ResultSet rs, DataSourceService dclpaDS) throws SQLException {
		this(manager,rs);
		this.dclpaDS = dclpaDS;
	}

	public DCLPAImage(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager);
		
		setSource(defaultSource);
		setClassifcation(DCLPAImage.CLASSIFICATION);
		int i = 1;
		
		setImageID(rs.getString(i++));
		setFilename(rs.getString(i++));
		setImgVolumePath(rs.getString(i++));
		
		// TODO - catalogued is a hack and might not even work - really, we need to be using some other form of trickery to process the last modified stuff
		// and it might be perfectly fine to not save the catalogued date and to instead just override the handling of the search based on 
		// a single field and to defer to the super.method for the rest if needs be
		setCatalogued(rs.getDate(i++));

		setWidth(				rs.getLong(i++));
		setHeight(				rs.getLong(i++));
		setFormatFromExtension(	rs.getString(i++));
		setThumbnailSize(		rs.getInt(i++));
		
		setTmsObjectID(			rs.getString(i++));
		setCreator(				rs.getString(i++));
		setOriginalSource(		rs.getString(i++));
		setOriginalSourceType(	rs.getString(i++));
		setViewDescription(		rs.getString(i++));
		setDescription(			rs.getString(i++));
		setProjectDescription(	rs.getString(i++));
		setLightQuality(		rs.getString(i++));
		setSpectrum(			rs.getString(i++));
		setTreatmentPhase(		rs.getString(i++));
		setOriginalFilename(	rs.getString(i++));
		setCaptureDevice(		rs.getString(i++));
		setOriginalSourceInstitution(	rs.getString(i++));
		setPhotographer(		rs.getString(i++));
		setProductionDate(		rs.getDate(i++));
		setProductType(			rs.getString(i++));
	}

    public DCLPAImage factory(ResultSet rs) throws SQLException {
        DCLPAImage d = new DCLPAImage(getManager(),rs);
        return d; 
    }
    
    public DCLPAImage(ArtDataManagerService manager) {
    	super(manager);
    }

    private void setCatalogued(Date catalogued) {
    	if (catalogued != null) {
    		DateTime dt = new DateTime(catalogued);
    		setCatalogued(dt.toString());
    	}
    }
  
    private void setTmsObjectID(String objectID) {
    	if (objectID != null) {
    		try {
    			Long id = Long.parseLong(objectID);
    			setTmsObjectID(id);
    		}
    		catch (NumberFormatException ne) {
    			log.warn("an object id (" + objectID + ") stored in the " + getSource() + " DAM for image ID (" + getImageID() + ") is not in the proper format");
    		}
    	}
    }

	// since we've mounted the DAR to the image repository server, the images are exposed via a web server
	// and through the path "/dar" so we have to sync that up with the paths stored in portfolio which are
	// formatted like this: ::Sv-nas-tdp:Group:DAR:DCLPA:Past_Years:2003:DCLPA_89671_20030522_00419.jpg
    @Override
	public void setImgVolumePath(String path) {
		//log.info("from:" + path);
		path = pathTransformationPattern.matcher(path).replaceAll("/dar$1");
		//log.info("to: " + path);
		super.setImgVolumePath(path);
		// TODO set it by parsing out the path
	}

	// no protocol relative URLs available for DCLPA images
    @Override
	public URI getProtocolRelativeiiifURL(String region, String size, String rotation, String quality) {
		return null;
	}

    // no facets for art object images... at least not yet
    @Override
	public List<String> getFacetValue(Object f) {
		return CollectionUtils.newArrayList();
	}

    @Override
	public Thumbnail getThumbnail(int width, int height, int maxdim, boolean exactSizeRequired, boolean preferBase64, String scheme) {
		
		// if caller needs the exact size and we don't have the exact size then they don't get a thumbnail
		if (exactSizeRequired && ( getThumbnailSize() != width || getThumbnailSize() != height) )
			return null;
		if (getThumbnailSize() > maxdim)
			return null;
		
		// otherwise, we'll give them what we've got and it will have to be base64 format since direct URL isn't practical
		// for non-zoom images, i.e. we don't have IIIF server and PTIF resources for DCLPA images so we have to load the 
		// image from portfolio's database directly - we get what we get and we don't get upset
		try ( Connection conn = dclpaDS.getConnection() ) {
			try ( Statement st = conn.createStatement() ) {
				try (ResultSet rs = st.executeQuery("SELECT value FROM blobs_table WHERE record_id=" + this.getImageID() + " AND field_id=12") ) {
					if (rs.next()) {
						Blob blob = rs.getBlob(1);
						Long len = blob.length();
						return new Thumbnail(blob.getBytes(1,len.intValue()));
					}
					rs.close();
				}
				st.close();
			}
			conn.close();
		}
		catch (SQLException se) {
			log.warn("Could not fetch thumbnail", se);
		}
		
		return null;
	}
	
}
