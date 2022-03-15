// TODO - in the future, re-architect this class to pull entity IDs from SOLR and then fetch those
// entities from the database in real-time rather than having to cache all the data in memory - so long
// as the AEM classes are changed to call the NGA data API implementation rather than caching the data
// in RAM themselves, I'm less concerned about this component's refactoring as in-memory operations are
// much faster and we don't have any real-time data synchronization requirements (yet).
/*
    NGA ART DATA API: CSpaceArtDataManager.java provides an extension to the ArtDataManager class
    that binds it to Spring configurations and creates it as a SpringBoot service so it can be 
    referenced / injected into other classes that need it.  This class is responsible for controlling
    the timing of loading / unloading the in-memory cache of all art entities from the database. 

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

package gov.nga.integration.cspace.tms;

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

import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.entities.art.ArtDataManager;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Derivative;
import gov.nga.integration.cspace.CSpaceTestModeService;
import gov.nga.integration.cspace.ImageThumbnailWorker;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.ConfigService;
import gov.nga.utils.DateUtils;
import gov.nga.utils.db.DataSourceService;

// spring defaults to singleton beans so that's good - nothing to spec here in that case
@Service
public class CSpaceArtDataManager extends ArtDataManager {
	
    private static final Logger log = LoggerFactory.getLogger(CSpaceArtDataManager.class);
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    @Autowired
    private ConfigService cs;

    @Autowired
    private CSpaceTestModeService ts;
    
    @Autowired
    private ArtDataCacher cacher;
    
    @Autowired
    private ArtDataQuerier querier;

    @Resource(name="nga.jdbc.tms") 
    private DataSourceService ds;
    
    @Resource(name="nga.jdbc.dclpa") 
    private DataSourceService portDB;

    @Resource(name="nga.jdbc.dclpa") 
    private DataSourceService dclpaDataSource;

    
    @PostConstruct
    public void postConstruct() {
    	
        // we probably don't really need to implement this
        // since we are going to asynchronously load the data
        // using the DataRefreshController
        log.info("******************************************* Activating Art Data Manager Service **********************************************");
        setConfigService(cs);
        setDataSourceService(ds);
        setArtDataCacher(cacher);
        setArtDataQuerier(querier);

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
    
    public void run() {

    	synchronized(loading) {
    		// if we're already loading in another thread, don't re-load
    		if (isLoading()) {
    			log.info("********************** Skipped Art Data Manager Service Refresh Schedule Since Already Running ************************");
    			return;
    		}
    		setLoading(true);
    	}
    	try {
    		// unload TMS data if already loaded
    		//setArtObjectsList(null);
    		if ( ts.unloadBeforeLoading() ) {
    			setDataReady(false);
    			unload();
    		}
    		
    		// TODO -- having to clear cache manually from here isn't the best design but for only one cache at this level, it's probably fine
    		// for now.  In future, probably a better pattern would be to implement a resetOnLoad interface and then find all classes implementing it
    		// and call the rest operation
    		if (!load()) {
    			// if we are unable to load, then we will try again in ten seconds
    			scheduler.schedule(this, 10, TimeUnit.SECONDS); 
    		}
    		sendMessage(EVENTTYPES.DATAREFRESHED);
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
        log.info("******************************************* Destroying Art Data Manager Service **********************************************");
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
    		
    		for (ArtObject o : this.getArtDataQuerier().fetchByObjectIDs(ids).getResults()) {
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
    		sendMessage(EVENTTYPES.DATAREFRESHED);
    		ImageThumbnailWorker.clearCache(); 
    	}
    }

    // reload TMS data periodically according to a cron schedule
    @Scheduled(cron="${ngaweb.CSpaceArtDataManager.refresh.cron}")
    public void refreshData() {
    	// TODO - rework this to support refreshing without unloading the existing data from memory
    	log.info("******************************************* Scheduled Refresh of TMS Data *****************************************************");
    	run();
    }

/*    private List<ArtObject> artObjectsList = null;
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
*/
}
