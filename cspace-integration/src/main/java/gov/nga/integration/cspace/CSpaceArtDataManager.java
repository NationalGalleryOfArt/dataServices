package gov.nga.integration.cspace;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gov.nga.entities.art.ArtDataManager;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.Derivative;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.ConfigService;
import gov.nga.utils.DateUtils;
import gov.nga.utils.db.DataSourceService;

@Service
public class CSpaceArtDataManager extends ArtDataManager {
	
    private static final Logger log = LoggerFactory.getLogger(CSpaceArtDataManager.class);
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    @Autowired
    private ConfigService cs;

    @Autowired
    private CSpaceTestModeService ts;

    @Resource(name="nga.jdbc.tms") 
    private DataSourceService ds;
    
    @Resource(name="nga.jdbc.dclpa") 
    private DataSourceService portDB;

    @Resource(name="nga.jdbc.dclpa") 
    private DataSourceService dclpaDataSource;

/*    private void doMyTest() {
    	log.trace("************************ STARTING PORTFOLIO SEARCH TESTS: ************************");
		long cTime = System.currentTimeMillis();
		long tTime = System.currentTimeMillis();
		
		List<CSpaceImage> imageHitList = CollectionUtils.newArrayList();
		
		// fill the object IDs with dummy data so they are compatible with the JDBC calls but won't impact anything
		byte[] objectIDs = new byte[4];
		ByteUtils.fillBytes(objectIDs, 0, Integer.MIN_VALUE);

		log.trace("************************ GETTING CONNECTION: ************************");
		try ( Connection conn = dclpaDataSource.getConnection() ) {

			log.trace("************************ PREPARING ARRAY: ************************");
			// PACK THE OBJECT IDS INTO A DENSE ARRAY OF BINARY DATA TO SEND TO A STORED PROCEDURE THAT PARSES THEM
			int size = 210000;
			byte[] bytes = new byte[(Integer.SIZE/8)*size];
			int j = 0;
			for (int i = 0; i < 210000; i++) {
				int id = i; 
				bytes[j++] = (byte) (id >>> 24);
				bytes[j++] = (byte) (id >>> 16);
				bytes[j++] = (byte) (id >>> 8);
				bytes[j++] = (byte) (id >>> 0);
			}
			// assign to the variable used in the stored procedure call 
			objectIDs = bytes;
			
			DateTime MINDATE = DateTime.parse("1754-01-01T01:00:00+00:00"); // SQL server supports a min date of 1/1/1753
			DateTime MAXDATE = DateTime.parse("9998-01-01T01:00:00+00:00"); // SQL server supports up to 12/31/9999 this
			DateTime beginDateRange = MINDATE;
			DateTime endDateRange = MAXDATE;

			log.trace("************************ PREPARING CALL: ************************");
			// Now, perform query of the database based on an ID that might have been provided to us 
			// or the lastModified date of the image which, in this case, we use the catalogued date in Portfolio
			String selectImagesSQL = new DCLPAImage(this).getAllImagesQuery();
			try ( PreparedStatement ps = conn.prepareCall(selectImagesSQL) ) {
				int p = 1;
				ps.setInt(p++, 0);
				ps.setDate(p++, new java.sql.Date(beginDateRange.toDate().getTime()));
				ps.setDate(p++, new java.sql.Date(endDateRange.toDate().getTime()));
				ps.setInt(p++, 0);
				ps.setInt(p++, 0);
				ps.setInt(p++, 1);
    			ps.setBlob(p++, new SerialBlob(objectIDs));

    			log.trace("************************ EXECUTING STORED PROCEDURE: ************************");
    			cTime = System.currentTimeMillis();
				try ( ResultSet rs = ps.executeQuery() ) {
					tTime = System.currentTimeMillis() - cTime;
					if (rs != null) {
						while (rs.next()) {
							// TODO - done already? I think the approach here should be to create a fully populated image (without all of the references, etc.) and then
							// dumb it down to create the abridged images in the overall response
							DCLPAImage image = new DCLPAImage(this,rs,dclpaDataSource,ts);
							imageHitList.add(image);
						}
					}
					rs.close();
				}
				ps.close();
			}
			// tTime = System.currentTimeMillis() - cTime; cTime = System.currentTimeMillis();
			log.trace("************************ TOTAL PORTFOLIO SEARCH EXECUTION TIME FOR " + imageHitList.size() + " IMAGES: " + tTime + " MS ************************");
			conn.close();
		}
    	catch (SQLException se) {
    		log.error(se.getMessage(),se);
    	}
    }
  */
    
    @PostConstruct
    public void postConstruct() {
    	
/*    	// test the performance of cspace search
    	for (int i = 0; i<5; i++)
    		doMyTest();
    	System.exit(1);
*/    	
    	
        // we probably don't really need to implement this
        // since we are going to asynchronously load the data
        // using the DataRefreshController
        log.info("AAAAAAAAAAAAAAAAAAAAAA: Activating Art Data Manager YEEHAAWW AAAAAAAAAAAAAAAAAAAAAAAA");
        setConfigService(cs);
        setDataSourceService(ds);

        // if we're unable to load, then we should try again every minute until we succeed
        scheduler.schedule(this, 0, TimeUnit.SECONDS);
        
    }
    
    private Boolean loading=false;
    synchronized void setLoading(Boolean loading) {
    	this.loading = loading;
    }
    private Boolean isLoading() {
    	return this.loading;
    }
    
    private boolean testApplied = false;
    
    public void run() {
    	synchronized(loading) {
    		// if we're already loading in another thread, don't re-load
    		if (isLoading())
    			return;
    		setLoading(true);
    	}
    	try {
    		// unload TMS data if already loaded
    		setArtObjectsList(null);
    		if ( !testApplied ) {
    			if ( ts.isTestModeHalfObjects() )
    				ArtObject.fetchAllObjectsQuery += " WHERE objectid <= 100000 ";
    			else if ( ts.isTestModeOtherHalfObjects() )
    				ArtObject.fetchAllObjectsQuery += " WHERE objectid > 100000 ";
    			testApplied = true;
    		}
    		unload();
    		// TODO -- having to clear cache manually from here isn't the best design but for only one cache at this level, it's probably fine
    		// for now.  In future, probably a better pattern would be to implement a resetOnLoad interface and then find all classes implementing it
    		// and call the rest operation
    		if (!load()) {
    			// if we are unable to load, then we will try again in ten seconds
    			scheduler.schedule(this, 10, TimeUnit.SECONDS); 
    		}
    		else
    			setArtObjectsList(getArtObjects());
    		ImageThumbnailWorker.clearCache(); 
    	}
    	catch (Exception e) {
    		log.error("Error loading data e",e);
    	}
    	finally {
    		setLoading(false);
    	}
    }
        
    // we unload all data upon destruction of this component
    @PreDestroy
    public void preDestroy() {
        log.info("AAAAAAAAAAAAAAAAAAAAAA: Destroying Art Data Manager");
        // unload art object data from memory
        unload();
    }

    @Scheduled(cron="0 */2 * * * *")
    public void updateTestData() {
    	if (ts.isTestModeHalfObjects() || ts.isTestModeOtherHalfObjects() ) {
    		log.info("Updating art objects with ids 5000-6000 and 105000-106000 with recent modification dates");
    		List<Long> ids = CollectionUtils.newArrayList();
    		for (long i=5000; i<6000; i++)
    			ids.add(i);
    		for (long i=105000; i<106000; i++)
    			ids.add(i);

    		String delimeter = " -=-=-=- ";
    		
    		List<ArtObject> objects = fetchByObjectIDs(ids);
    		for (ArtObject o : objects) {
    			if (o != null) {
    				o.setLastDetectedModification(DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, new Date()));
    				String t = o.getTitle();
    				if (t == null)
    					t = "";
    				String[] parts = t.split(delimeter);
    				t = parts[0];
    				o.setTitle(t + delimeter + "[" + o.getLastDetectedModification() + "]");

    				// and now adjust any primary images that are associated with this object  
    				List<Derivative> dList = o.getImages();
    				if (dList != null) {
    					for (Derivative d : dList) {
   							d.setCatalogued(DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, new Date()));
   							d.setTestingMessage(d.getCatalogued());
    					}
    				}
    				
    			}
    		}
    	}
    }

    // reload TMS data every 10 minutes
    // TODO merge this cron with an application property
    @Scheduled(cron="0 0 8 * * *")
//    @Scheduled(cron="0 */2 * * * *") // for testing a problem encountered during refresh under load
    public void refreshData() {
    	// TODO - rework this to support refreshing without unloading the existing data from memory
    	log.info("****************** REFRESH OF TMS DATA RUNNING **********************");
    	run();
    }

    private List<ArtObject> artObjectsList = null;
    private synchronized void setArtObjectsList(List<ArtObject> artObjectsList) {
    	this.artObjectsList = artObjectsList;
    }
    
    @Override // overriding for caching performance
    public List<ArtObject> getArtObjects() {
    	if (artObjectsList != null)
    		return artObjectsList;
    	else
    		return super.getArtObjects();
    }

}
