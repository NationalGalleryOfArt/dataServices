package gov.nga.integration.cspace;

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
import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;

@Service
public class CSpaceArtDataManager extends ArtDataManager {
	
    private static final Logger log = LoggerFactory.getLogger(CSpaceArtDataManager.class);
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    @Autowired
    private ConfigService spring_configurator;

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
        setConfigService(spring_configurator);
        setDataSourceService(ds);

        // if we're unable to load, then we should try again every minute until we succeed
        scheduler.schedule(this, 0, TimeUnit.SECONDS);
        
    }
    
    public void run() {
    	// unload TMS data if already loaded
    	setArtObjectsList(null);
    	unload();
    	// TODO -- having to clear cache manually from here isn't the best design but for only one cache at this level, it's probably fine
    	// for now.  In future, probably a better pattern would be to implement a resetOnLoad interface and then find all classes implementing it
    	// and call the rest operation
        ImageThumbnailWorker.clearCache();
    	if (!load()) {
    		// if we are unable to load, then we will try again in one minute
    		scheduler.schedule(this, 10, TimeUnit.SECONDS); 
    	}
    	else
    		setArtObjectsList(getArtObjects());
    }
        
    // we unload all data upon destruction of this component
    @PreDestroy
    public void preDestroy() {
        log.info("AAAAAAAAAAAAAAAAAAAAAA: Destroying Art Data Manager");
        // unload art object data from memory
        unload();
    }
    
    // reload TMS data every 10 minutes
    // TODO merge this cron with an application property
    @Scheduled(cron="0 0 8 * * *")
    public void refreshData() {
    	// TODO - rework this to support refreshing without unloading the existing data from memory
    	log.info("****************** REFRESH OF TMS DATA RUNNING **********************");
    	scheduler.schedule(this, 0, TimeUnit.SECONDS);
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
