package gov.nga.entities.art;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtData;
import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.common.entities.art.ArtDataManagerSubscriber;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.ArtObject.FACET;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.StringUtils;
import gov.nga.common.utils.SystemUtils;
import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.DataNotReadyException;
import gov.nga.common.entities.art.MessageProvider;
import gov.nga.common.entities.art.MessageProviderImpl;
import gov.nga.common.entities.art.OperatingMode;
import gov.nga.common.entities.art.TMSFetcher;
import gov.nga.common.entities.art.TMSFetcher.TMSData;
import gov.nga.common.search.Facet;
import gov.nga.common.search.FacetHelper;
import gov.nga.common.search.SearchHelper;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;
import gov.nga.entities.art.datamanager.services.ArtDataCache;

public class ArtDataManager  extends MessageProviderImpl implements Runnable, ArtDataManagerService, MessageProvider, OperatingModeService
{
    private static final Logger log = LoggerFactory.getLogger(ArtDataManager.class);
    private static final int CONTENT_SYNC_DELAY = 15;
    
	public ArtDataQuerier artQuerier;
	@Override
	public ArtDataQuerier getArtDataQuerier() {
		return artQuerier;
	}
	public void setArtDataQuerier(final ArtDataQuerier q) {
		artQuerier = q;
	}
	
	public ArtDataCacher datacacher;
	@Override
	public ArtDataCacher getArtDataCacher() {
		return datacacher;
	}
	public void setArtDataCacher(final ArtDataCacher cache) {
		datacacher = cache;
	}

    private boolean dataReady=false;
    synchronized protected void setDataReady(boolean dataReady) {
        this.dataReady = dataReady;
    }
	@Override
    public boolean isDataReady(boolean raiseException) {
    	if (!datacacher.getIsDataReady() && raiseException)
    		throw new DataNotReadyException("TMS data services are starting up and currently unavailable.");
    	return datacacher.getIsDataReady();
    }
	
    public DataSourceService dataSourceService;
    @Override
    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }
    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public ConfigService configurator;
    @Override
    public ConfigService getConfig() {
        return configurator;
    }
    protected void setConfigService(ConfigService configurator) {
        this.configurator = configurator;
    }
    
    public final String operatingModePropertyName = "operatingMode"; 
    @Override
    public OperatingMode getOperatingMode() {
    	String opmode = getConfig().getString(operatingModePropertyName);
    	if (StringUtils.isNotBlank(opmode) && opmode.equals(OperatingMode.PRIVATE.toString()) )
    		return OperatingMode.PRIVATE;
    	else
    		return OperatingMode.PUBLIC;
    }

    private volatile Long synchronizationFinishedAt = -1L;

    @Override
    public Long synchronizationFinishedAt() {
        return this.synchronizationFinishedAt;
    } 

    synchronized public void unload() {
        log.info("**************************** Unloading Previous Art Data Manager cached data--- *************************************************");
        setDataReady(false);
        setStandardArtObjectFacets(null);
        setAllIndexOfArtistRanges(null);
        ((ArtDataCache)datacacher).unload();
//        clearDerivativesByImageID();
        System.gc();
        log.info(SystemUtils.freeMemorySummary());
//        clearDerivativesRaw();
    }

    synchronized public boolean load() {

    	log.info("**************************** Starting Load of Refreshed Art Data Manager Cached Data Set *************************************");

        // load all art object constituent data first, then pass that
        // map to both the object manager and constituent manager
        try {
            ArtData newData = new TMSFetcher(getDataSourceService(), OperatingMode.PRIVATE, this).load();
            ((ArtDataCache)datacacher).artDataReady(newData);
            setDataReady(true);
            // pre-calculate all art object facet counts for use by the initial visual browser page
            log.info("Pre-caching all art object facet counts");
            getArtObjectFacetCounts();

            // pre-calculate the facet ranges for the index of artists
            log.info("Pre-caching all facet ranges for index of artists");
            getIndexOfArtistsRanges();
            log.info("**************************** Finished Loading Art Data Manager Cached Data Set *******************************************");
            log.info(SystemUtils.freeMemorySummary());
            log.info("Returning true");
            synchronizationFinishedAt = Calendar.getInstance().getTimeInMillis();
            return true;
        }
        // if we are unable to fetch the data we need, then we reschedule
        // ourselves which will attempt to kick off another refresh immediately
        catch (final Exception se) {
            log.error("ERROR Loading TMS Data:: " + se.getMessage(), se );
        }
        return false;
    }

    public Map<String, String> getIndexOfArtistsRanges() {

        if (allIndexOfArtistRanges == null){

            // setup our facet helper to calculate all art object facets
            FacetHelper fn = new FacetHelper(
                    Constituent.FACET.INDEXOFARTISTS_FIRST_TWO_LETTERS_LAST_NAME
            );

            // our search helper, instantiated according to the art entity we
            // intend to search against
            SearchHelper<Constituent> sh = new SearchHelper<Constituent>();
            this.getArtDataQuerier().searchConstituents(sh,null,fn);
            List<Facet> list = fn.getFacets();
            if (list != null && list.size() > 0) {
                Facet f = list.get(0);
                Map<String, Integer> m = f.getFacetCounts();
                Long sum = Long.valueOf(0);
                for (Integer i : m.values())
                    sum += i;
                long bucketSize = sum / 25;

                Map<String, String> newIndexOfArtistsRanges = CollectionUtils.newTreeMap(Facet.facetSorter);
                sum = Long.valueOf(0);
                String start = null;
                String end = null;
                for (String s : m.keySet()) {
                    if (sum == 0) {
                        start = s;
                        end = s;
                    }
                    sum += m.get(s);
                    if (sum >= bucketSize) {
                        newIndexOfArtistsRanges.put(start, s);
                        sum = Long.valueOf(0);
                    }
                    end = s;
                }
                // if we still have some remaining
                if (sum > 0)
                    newIndexOfArtistsRanges.put(start, end);

                setAllIndexOfArtistRanges(newIndexOfArtistsRanges);
            }
        }
        return allIndexOfArtistRanges;
    }
    private Map<String, String> allIndexOfArtistRanges;
    synchronized private void setAllIndexOfArtistRanges(Map<String, String> allIndexOfArtistRanges) {
        this.allIndexOfArtistRanges = allIndexOfArtistRanges;
    }

    public List<Facet> getArtObjectFacetCounts() throws DataNotReadyException {
        if (standardArtObjectFacets == null) {
            // setup our facet helper to calculate all art object facets
            FacetHelper fn = new FacetHelper(
                FACET.VISUALBROWSERCLASSIFICATION,
                FACET.VISUALBROWSERNATIONALITY,
                FACET.VISUALBROWSERTIMESPAN,
                FACET.VISUALBROWSERTHEME,
                FACET.VISUALBROWSERSTYLE,
                FACET.SCHOOL,
                FACET.ONVIEW
            );

            // our search helper, instantiated according to the art entity we
            // intend to search against
            SearchHelper<ArtObject> sh = new SearchHelper<ArtObject>();
            this.getArtDataQuerier().searchArtObjects(sh,null,fn);
            setStandardArtObjectFacets(fn.getFacets()); 
        }
        return standardArtObjectFacets;
    }
    
    private List<Facet> standardArtObjectFacets;
    synchronized private void setStandardArtObjectFacets(List<Facet> standardArtObjectFacets) {
        this.standardArtObjectFacets = standardArtObjectFacets;
    }

    
	@Override
    synchronized public void run()  {
        log.info("Refreshing TMS in-memory cache -- QUARTZ start");
        
        // first, we unload our existing data cache to free up memory
        // if our production instance has plenty of memory, we can opt to
        // move this just prior to setting the data (inside load) which 
        // will decrease the down time
         
        boolean loaded = false;
        try {
            // do not clear existing TMS data as it may break search (and other components) while refreshing data;
            // leave old data in place and replace it with new data as soon as the new data is loaded into memory
            //unload();
            // then attempt to load art data
            try {
                loaded = load();
                if(loaded){
                    this.synchronizationFinishedAt = System.currentTimeMillis(); 
                    try {
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.MINUTE, CONTENT_SYNC_DELAY);
          //              scheduler.unschedule(TMS_ART_DATA_JCR_SYNC);
          //              scheduler.schedule(new ArtObjectJCRSynchronizer(this), scheduler.AT(c.getTime()).name(TMS_ART_DATA_JCR_SYNC));
                        log.info("**** CONTENT SYNC SCHEDULED IN " + CONTENT_SYNC_DELAY + " MINUTES ****");
                    }
                    catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            } 
            catch (Exception ex) {
            	log.error("Could not load art object data, but will retry", ex);
                loaded = false;
            }
        }
        finally {
            if (!loaded) {
    			unload();
                // attempt to reschedule the bundle load in 30 seconds
                // since we failed to load the first time
                try { 
                    log.info("Loading TMS art object data will retry in 30 seconds");
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, 30);
            //        scheduler.unschedule(TMS_BUNDLE_REFRESH);
            //        scheduler.schedule(this, scheduler.AT(c.getTime()).name(TMS_BUNDLE_REFRESH));
                }
                catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            log.info("Refreshing TMS in-memory cache -- QUARTZ stop");
        }
    }

}
