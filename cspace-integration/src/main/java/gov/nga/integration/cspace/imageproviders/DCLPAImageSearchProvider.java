package gov.nga.integration.cspace.imageproviders;

import java.sql.Connection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialBlob;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.OperatingModeService.OperatingMode;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Derivative;
import gov.nga.integration.cspace.CSpaceImage;
import gov.nga.integration.cspace.CSpaceTestModeService;
import gov.nga.search.SearchFilter;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.utils.ByteUtils;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.db.DataSourceService;

// we have to register this with Spring in order to use Spring's bean services to get access to it later as an implementer
// in this case, it doesn't really matter technically whether it's a generic component, a bean, a service, etc. but 
// since it most closely resembles a service, we'll use that component type 
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)  // default is singleton, but best to be explicit
public class DCLPAImageSearchProvider extends SourceProviderImpl {

	private static final String[] providesSource = {"portfolio-dclpa"};
	
	private static final Logger log = LoggerFactory.getLogger(DCLPAImageSearchProvider.class);
	
	// TODO put in a SQL Server DB constants utility class
	public static final DateTime MINDATE = DateTime.parse("1754-01-01T01:00:00+00:00"); // SQL server supports a min date of 1/1/1753
	public static final DateTime MAXDATE = DateTime.parse("9998-01-01T01:00:00+00:00"); // SQL server supports up to 12/31/9999 this
		
    @Resource(name="nga.jdbc.dclpa") 
    private DataSourceService dclpaDataSource;
	
	@Autowired
	private ArtDataManagerService artDataManager;
	
	@Autowired
	CSpaceTestModeService ts;

	// TODO the image record controller should be modified to use search with a specific source rather than a dedicated fetch
	public List<CSpaceImage> searchImages(
			SearchHelper<CSpaceImage> imageSearchHelper,
			List<ArtObject> limitToTheseArtObjects) throws SQLException {

		List<CSpaceImage> images = CollectionUtils.newArrayList();

		// acquire a connection to Portfolio from the connection pool
		
		// if we are to limit the images to specific objects then we can do that although the number of objects is potentially fairly large
		// so we'll have to see how it goes - might have to create a temp table, insert the objects of interest and perform a join
		
		// only return DCLPA images if we're operating in PRIVATE mode 
		if ( artDataManager.getOperatingMode() == OperatingMode.PRIVATE)
			images = fetchImages(limitToTheseArtObjects, imageSearchHelper);
		
    	return images;
	}

	//TODO - name and filename will come from image:filename and image:name but filename will probably be removed at some point
	// use the filename for image:title when available - actually we talked about using title as title of the artwork right?
	protected List<CSpaceImage> fetchImages(List<ArtObject> limitToTheseObjects, SearchHelper<CSpaceImage> searchHelper) throws SQLException {
		
		List<CSpaceImage> imageHitList = CollectionUtils.newArrayList();

		// perform some validation on the input values first
		DateTime beginDateRange = MINDATE;
		DateTime endDateRange = MAXDATE;
		int imageIdToSearch = Integer.MIN_VALUE;
		int searchByDates = 0;
		int searchById = 0;

		// iterate the search filters for images looking for values that we know how to handle and set the variables to add to the SQL query
		Set<SearchFilter> filters = searchHelper.getFilters();
		if (filters != null && filters.size() > 0) {
			for (SearchFilter sf : filters) {
				if (sf.getField().equals(Derivative.SEARCH.CATALOGUED) && sf.getOp().equals(SEARCHOP.BETWEEN)) {
					List<String> searchVals = sf.getStringSearchValues();
					// check bounds of the given dates to make sure we're not searching for 
					beginDateRange = DateTime.parse(searchVals.get(0));
					if (beginDateRange.isBefore(MINDATE))
						beginDateRange = MINDATE;
					endDateRange = DateTime.parse(searchVals.get(1));
					if (endDateRange.isAfter(MAXDATE))
						endDateRange = MAXDATE;
					// no point in continuing since the provided dates cannot possibly return any hits from a SQL Server database's dates
					if (beginDateRange.isAfter(MAXDATE) || endDateRange.isBefore(MINDATE))
						return imageHitList;
					searchByDates = 1;
				}
				else if (sf.getField().equals(Derivative.SEARCH.IMAGEID) && sf.getOp().equals(SEARCHOP.EQUALS)) {
					// validate that the id passed actually looks like a number otherwise, it's not going to match anything in our IDs anyway
					String id = sf.getStringSearchValues().get(0);
					try {
						imageIdToSearch = Integer.parseInt(id);
					}
					catch (NumberFormatException nfe) {
						// if and ID was supplied for the query but in a non-int format, then no point in continuing since we won't have any records
						return imageHitList; 
					}
					searchById = 1;
				}
			}
		}
		
		long cTime = System.currentTimeMillis();
		long tTime = System.currentTimeMillis();
		
		boolean limit = false;
		// fill the object IDs with dummy data so they are compatible with the JDBC calls but won't impact anything
		byte[] objectIDs = new byte[4];
		ByteUtils.fillBytes(objectIDs, 0, Integer.MIN_VALUE);

		try ( Connection conn = dclpaDataSource.getConnection() ) {
			
			// PACK THE OBJECT IDS INTO A DENSE ARRAY OF BINARY DATA TO SEND TO A STORED PROCEDURE THAT PARSES THEM
			if (limitToTheseObjects != null) {
				// return no images if there was a constraint on the objects but no objects were returned in the search
				if (limitToTheseObjects.size() < 1)
					return imageHitList;
				limit = true;
				int size = limitToTheseObjects.size();
				byte[] bytes = new byte[(Integer.SIZE/8)*size];
				int j = 0;
				for (ArtObject o : limitToTheseObjects) {
					int id = o.getObjectID().intValue();
					bytes[j++] = (byte) (id >>> 24);
					bytes[j++] = (byte) (id >>> 16);
					bytes[j++] = (byte) (id >>> 8);
					bytes[j++] = (byte) (id >>> 0);
				}
				// assign to the variable used in the stored procedure call 
				objectIDs = bytes;
			}
				
			// Now, perform query of the database based on an ID that might have been provided to us 
			// or the lastModified date of the image which, in this case, we use the catalogued date in Portfolio
			String selectImagesSQL = new DCLPAImage(artDataManager).getAllImagesQuery();
			try ( PreparedStatement ps = conn.prepareCall(selectImagesSQL) ) {
				int p = 1;
				ps.setInt(p++, searchByDates);
				ps.setDate(p++, new Date(beginDateRange.toDate().getTime()));
				ps.setDate(p++, new Date(endDateRange.toDate().getTime()));
				ps.setInt(p++, searchById);
				ps.setInt(p++, imageIdToSearch);
				ps.setInt(p++, limit ? 1: 0);
    			ps.setBlob(p++, new SerialBlob(objectIDs));

				try ( ResultSet rs = ps.executeQuery() ) {
					if (rs != null) {
						while (rs.next()) {
							// TODO - done already? I think the approach here should be to create a fully populated image (without all of the references, etc.) and then
							// dumb it down to create the abridged images in the overall response
							DCLPAImage image = new DCLPAImage(artDataManager,rs,dclpaDataSource,ts);
							imageHitList.add(image);
						}
					}
					rs.close();
				}
				ps.close();
			}
			tTime = System.currentTimeMillis() - cTime; cTime = System.currentTimeMillis();
			log.trace("************************ END TEMP TABLE: " + tTime + " ************************");
			conn.close();
		}
//    	catch (SQLException se) {
//  		log.error(se.getMessage(),se);
//    	}

		return imageHitList;
	}

	public String[] getProvidedSources() {
		return providesSource;
	}
	
}
