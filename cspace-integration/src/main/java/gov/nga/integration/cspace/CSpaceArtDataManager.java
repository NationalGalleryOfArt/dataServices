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

    @PostConstruct
    public void postConstruct() {
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
