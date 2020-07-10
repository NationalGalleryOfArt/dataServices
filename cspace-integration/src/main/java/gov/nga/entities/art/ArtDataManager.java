/*
    NGA Art Data API: Art Data Services Implementation
    Implementation of ArtDataManagerService. This implementation reads all data from
    RDBMS' and caches it in memory.  An alternative lighter weight implementation will
    make calls to a RESTful JSON implementation (probably initially of the cached 
    implementation to avoid having to cache the objects on multiple servers).  A third
    implementation might implement searches using SOLR and then read the art entity data
    from RDBMS' and other data sources in real-time.   

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
package gov.nga.entities.art;


import gov.nga.entities.art.ArtObject.FACET;
import gov.nga.entities.art.TMSFetcher.TMSData;
import gov.nga.entities.art.factory.ArtObjectFactory;
import gov.nga.entities.art.factory.ArtObjectFactoryImpl;
import gov.nga.entities.art.factory.ConstituentFactory;
import gov.nga.entities.art.factory.ConstituentFactoryImpl;
import gov.nga.search.Facet;
import gov.nga.search.FacetHelper;
import gov.nga.search.FreeTextSearchable;
import gov.nga.search.ResultsPaginator;
import gov.nga.search.SearchHelper;
import gov.nga.search.SortHelper;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.utils.ConfigService;
import gov.nga.utils.MutableInt;
import gov.nga.common.utils.StringUtils;
import gov.nga.utils.SystemUtils;
import gov.nga.utils.db.DataSourceService;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.suggest.Suggest;
import gov.nga.common.suggest.Suggestion;

import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// import com.day.cq.tagging.JcrTagManagerFactory;

// scheduler.expression will run the Runnable's run() at the default schedule
// specified below - activate method also kicks off the job immediately
// I had to add the sling commons scheduler jar (version 2.2.3)
// stolen from bundle 101 in order to get CRXDE command line completion  
// to work properly
//    @Property(name="scheduler.concurrent", boolValue=false, propertyPrivate=true),
//    @Property(name="scheduler.expression", value="0 15 7 * * ? *", label="Refresh Schedule")
public class ArtDataManager extends MessageProviderImpl implements Runnable, ArtDataManagerService, MessageProvider, OperatingModeService { 

// TODO - separate all JCR related calls from the base ArtDataManager to keep it pure and simple and create new art data manager CQ implementation wrapper for OSGI
// TODO - add a TMS basics JAR dependency in CQ that is generated from a separate GIT repository and simply provided to the AEM team

    private static final int CONTENT_SYNC_DELAY = 15;
//	private static final String TMS_BUNDLE_REFRESH = "tmsBundleRefresh";
//	private static final String TMS_ART_DATA_JCR_SYNC = "tmsArtDataJCRSync";

	private static ArtObjectFactory<ArtObject> artObjFactory = new ArtObjectFactoryImpl();
    
    private static ConstituentFactory<Constituent> constFactory = new ConstituentFactoryImpl();

    /*
    private static Comparator<Suggestion> suggestionAlphaDiacriticNormalizedComparator =
        new Comparator<Suggestion>() {
            public int compare(Suggestion a, Suggestion b) {
                int j = StringUtils.getDefaultCollator().compare(a.string, b.string);
                return j;
            }
        };
    public class Suggestion {
        private Long entityID = null;
        private String string = null;
        private Suggestion(String string, Long entityID) {
            this.string = string;
            this.entityID = entityID;
        }
        
        public String getString() {
            return string;
        }
        
        public Long getEntityID() {
            return entityID;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Suggestion) {
                Suggestion other = (Suggestion) o;
                if (TypeUtils.compare(other.entityID, entityID) != 0)
                    return false;
                if (TypeUtils.compare(other.string, string) != 0)
                    return false;
                return true;
            }
            return false;
        }
        
        public int hashCode() {
            if (entityID == null && string == null)
                return 0;
            if (string == null)
                return Long.valueOf(entityID).hashCode();
            return (Long.valueOf(entityID).hashCode() >> 13) ^ string.hashCode();
        };
    }
	*/
    private static final Logger log = LoggerFactory.getLogger(ArtDataManager.class);

    // cache of art object data
    private Map<Long, ArtObject> artObjects = null; 
    private List<Facet> standardArtObjectFacets = null;
    private Map<String, Set<Suggestion>> artObjectTitleWords = null; 

    // cache of constituent data
    private Map<Long, Constituent> constituents = null;
    private Map<String, String> allIndexOfArtistRanges = null;
    private Map<String, Set<Suggestion>> artistAltNames = null; 
    private Map<String, Set<Suggestion>> ownerAltNames = null; 

    // cache of location data
    private Map<Long, Location> locations = null;
    
    // cache of exhibition data
    private Map<Long, Exhibition> exhibitions = null;

    // cache of web defined location / place definitions which are
    // also mapped to visual maps and TMS location IDs
    private Map<String, Place> places = null;
    private Map<Long, Place> placesTMSLocations = null;
    
    private Map<Long, Media> mediaItems = null;
    private Map<String, List<Media>> mediaRelationships = null;
    
    public DataSourceService dataSourceService;
    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }
    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public ConfigService configurator;
    public ConfigService getConfig() {
        return configurator;
    }
    protected void setConfigService(ConfigService configurator) {
        this.configurator = configurator;
    }
    
    public final String operatingModePropertyName = "operatingMode"; 
    public OperatingMode getOperatingMode() {
    	String opmode = getConfig().getString(operatingModePropertyName);
    	if (StringUtils.isNotBlank(opmode) && opmode.equals(OperatingMode.PRIVATE.toString()) )
    		return OperatingMode.PRIVATE;
    	else
    		return OperatingMode.PUBLIC;
    }

    private volatile Long synchronizationFinishedAt = -1L;

    public Long synchronizationFinishedAt() {
        return this.synchronizationFinishedAt;
    } 

    private boolean dataReady=false;
    synchronized protected void setDataReady(boolean dataReady) {
        this.dataReady = dataReady;
    }

    public boolean isDataReady(boolean raiseException) {
    	if (!dataReady && raiseException)
    		throw new DataNotReadyException("TMS data services are starting up and currently unavailable.");
    	return dataReady;
    }
     
 /*   
  * private class ArtObjectJCRSynchronizer implements Runnable {
    	ArtDataManager manager = null;
    	protected ArtObjectJCRSynchronizer(ArtDataManager manager) {
    		this.manager = manager;
		}
    	synchronized public void run() {
    		manager.synchronizeJCR();
    	}
    }
*/
    
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

    synchronized public void unload() {
        log.info("**************************** Unloading Previous Art Data Manager cached data--- *************************************************");
        setDataReady(false);
        setArtObjects(null);
        setLocations(null);
        setConstituents(null);
        setStandardArtObjectFacets(null);
        setAllIndexOfArtistRanges(null);
//        clearDerivativesByImageID();
        setArtObjectTitleWords(null);
        setArtistAltNames(null);
        setOwnerAltNames(null);
        setSuggestionManager(null);
        System.gc();
        log.info(SystemUtils.freeMemorySummary());
//        clearDerivativesRaw();
    }

    synchronized public boolean load() {

    	log.info("**************************** Starting Load of Refreshed Art Data Manager Cached Data Set *************************************");

        // load all art object constituent data first, then pass that
        // map to both the object manager and constituent manager
        try {
            TMSData newData = new TMSFetcher(getDataSourceService(), OperatingModeService.OperatingMode.PRIVATE, this).load();
            
            log.info("Computing art object title words and constituent altnames for suggest feature");
            final SuggestionManager sMgr = new SuggestionManager();
            parseArtObjectTitleWords(newData.artObjects, sMgr);
        	parseExhibitions(newData.exhibitions, sMgr);
        	parseConstituents(newData.constituents, sMgr);
        	log.info(String.format("Suggestion Manager built: %s", sMgr));
            log.info(SystemUtils.freeMemorySummary());
        	// dump any cached data we might still have before setting the new set in place  

			unload();
        	log.info("Cache cleared, setting new values");
        	setLocations(newData.locations);
        	setPlaces(newData.newPlaces);
        	setPlacesTMSLocations(newData.newPlacesTMSLocations);
        	setMediaItems(newData.newMediaItems);
        	setMediaRelationships(newData.newMediaRelationshps);
        	setArtObjects(newData.artObjects);
        	setConstituents(newData.constituents);
        	setExhibitions(newData.exhibitions);
        	setSuggestionManager(sMgr);
        	log.info("Data refresh complete. Ready to serve queries.");
            // we can start serving queries again now
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
            return true;
        }
        // if we are unable to fetch the data we need, then we reschedule
        // ourselves which will attempt to kick off another refresh immediately
        catch (final Exception se) {
            log.error("ERROR Loading TMS Data:: " + se.getMessage(), se );
        }
        return false;
    }

    @Deprecated
    public Narrative loadNarrative(long id, String query) {
        try {
            EntityQuery<Narrative> eq = new EntityQuery<Narrative>(getDataSourceService());
            return eq.fetchAndCreate(id, query, new Narrative(this));
        }
        catch (SQLException se) {
            log.error("Error loading narrative: " + se.getMessage());
        }
        return null;
    }

    public ArtObject fetchByObjectID(long objectID) throws DataNotReadyException {
        return fetchByObjectID(objectID, artObjFactory);
    }
    

    public <T extends ArtObject>T fetchByObjectID (long objectID, ArtObjectFactory<T> factory) throws DataNotReadyException {
    	isDataReady(true);
    	if( getArtObjectsRaw().get(objectID) == null ) {
    		return null;
    	}
    	else {
    		return factory.createArtObject(getArtObjectsRaw().get(objectID));
    	}
    }

    public List<ArtObject> fetchByObjectIDs(Collection<Long> objectIDs, gov.nga.entities.art.ArtObject.SORT... order) throws DataNotReadyException {
        return fetchByObjectIDs(objectIDs, artObjFactory, order);
    }

    public <T extends ArtObject>List<T> fetchByObjectIDs(Collection<Long> objectIDs, ArtObjectFactory<T> factory, gov.nga.entities.art.ArtObject.SORT... order) throws DataNotReadyException {
        List<T> list = fetchByObjectIDs(objectIDs, factory);

        // sort the list of objects before returning it
        SortHelper<T> sh = new SortHelper<T>();
        sh.sortArtEntities(list, (Object[]) order);

        return list;
    }
    
    public List<ArtObject> fetchByObjectIDs(Collection<Long> objectIDs) throws DataNotReadyException {
        return fetchByObjectIDs(objectIDs, artObjFactory);
    }

    public <T extends ArtObject>List<T> fetchByObjectIDs(Collection<Long> objectIDs, ArtObjectFactory<T> factory) throws DataNotReadyException {
        isDataReady(true);

        List<T> have = CollectionUtils.newArrayList();

        if (objectIDs != null) {
            for (Long id : objectIDs) {
                T o = factory.createArtObject(getArtObjectsRaw().get(id));
                have.add(o);
            }
        }
        return have;
    }

    // returns all works related to this one
    public <T extends ArtObject>List<T> fetchRelatedWorks(ArtObject obj, ArtObjectFactory<T> factory) throws DataNotReadyException {
        isDataReady(true);

        List<T> returnList = CollectionUtils.newArrayList();
        if (obj instanceof ArtObject)
        {
            ArtObject baseO = (ArtObject)obj;
        
            Map<String, MutableInt> nationalities = baseO.getNationalities();
            Map<T, Long> m = CollectionUtils.newHashMap();
            ArtObjectMapComparator<T> smc = new ArtObjectMapComparator<T>(m);
            Map<T, Long> sm = CollectionUtils.newTreeMap(smc);
    
            if (baseO != null) {
                // common styles
                for (ArtObject o : getArtObjectsRaw().values()) 
                {
                    if (!baseO.getObjectID().equals(o.getObjectID()))
                    {
                        Long score = baseO.relatedTotalScore(o, nationalities);
                        if (score > 0)
                        {
                            m.put(factory.createArtObject(o), score);
                        }
                    }
                }
    
                // now, populate the sortable map
                sm.putAll(m);
    
                returnList = CollectionUtils.newArrayList(sm.keySet());
            }
        }
            
        return returnList;
    }
    
    public <T extends ArtObject> List<T> searchArtObjects(SearchHelper<T> searchH, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH, ArtObjectFactory<T> factory, FreeTextSearchable<T> freeTextSearcher) throws DataNotReadyException {
        isDataReady(true);

        List<T> list = new ArrayList<T>();
        for (ArtObject obj: getArtObjectsRaw().values())
        {
            list.add(factory.createArtObject(obj));
        }
        searchH.setFreeTextServicer(freeTextSearcher);
        return searchH.search(list, pn, fn, sortH);
    }

    public <T extends ArtObject>List<T> searchArtObjects(SearchHelper<T> searchH, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH, ArtObjectFactory<T> factory) throws DataNotReadyException {
        return searchArtObjects(searchH, pn, fn, sortH, factory, new ArtEntityFreeTextSearch<T>());
    }
    
    public List<ArtObject> searchArtObjects(SearchHelper<ArtObject> searchH, ResultsPaginator pn, FacetHelper fn, SortHelper<ArtObject> sortH) throws DataNotReadyException {
        return searchArtObjects(searchH, pn, fn, sortH, artObjFactory);
    }

    public <T extends ArtObject>List<T> searchArtObjects(SearchHelper<T> searchH, ResultsPaginator pn, FacetHelper fn,
            ArtObjectFactory<T> factory, FreeTextSearchable<T> freeTextSearcher, Object... order) throws DataNotReadyException {
        SortHelper<T> sh = null;
        if (order != null && order.length > 0) {
            sh = new SortHelper<T>();
            sh.setSortOrder(order);
        }
        return searchArtObjects(searchH, pn, fn, sh, factory, freeTextSearcher);
    }

    public <T extends ArtObject>List<T> searchArtObjects(SearchHelper<T> searchH, ResultsPaginator pn, FacetHelper fn,
            ArtObjectFactory<T> factory, Object... order) throws DataNotReadyException {
        return searchArtObjects(searchH, pn, fn, factory, new ArtEntityFreeTextSearch<T>(), order);
    }

    public List<ArtObject> searchArtObjects(SearchHelper<ArtObject> searchH, ResultsPaginator pn, FacetHelper fn, Object... order) throws DataNotReadyException {
        return searchArtObjects(searchH, pn, fn, artObjFactory, order);
    }

    public List<ArtObject> fetchObjectsByRelationships(List<ArtObjectConstituent> ocs) {
        return fetchObjectsByRelationships(ocs, artObjFactory);
    }

    // simply assemble a map of objects that all the given constituents have in common
    // and return the values as a list
    public <T extends ArtObject>List<T> fetchObjectsByRelationships(List<ArtObjectConstituent> ocs, ArtObjectFactory<T> factory) {
        Map<Long, T> aos = CollectionUtils.newHashMap();
        if (ocs != null) {
            for (ArtObjectConstituent oc : ocs) {
                Constituent c = oc.getConstituent();
                if (c != null) {
                    List<ArtObject> works = c.getWorks();
                    for (ArtObject o : works) {
                        aos.put(o.getObjectID(), factory.createArtObject(o));
                    }
                }
            }
        }
        return CollectionUtils.newArrayList(aos.values());
    }

    /*  protected void fetchConstituentRolesForObjects(List<Long> objectIDs) throws SQLException {
        // determine which objects in the supplied list we don't have constituents loaded for yet
        List<Long> ids = CollectionUtils.newArrayList(); 
        for (Long id : objectIDs) {
            // presumably these objects have already been loaded by the caller so we won't
            // incur a performance hit here
            ArtObject o = fetchByObjectID(id); // fetch the associated object
            if (o != null && !o.areConstituentsLoaded())
                ids.add(id);
        }

        // nothing to do - already cached
        if (ids.size() < 1)
            return;

        EntityQuery<ArtObjectConstituent> eq = new EntityQuery<ArtObjectConstituent>(getPoolService());
        List<ArtObjectConstituent> list = CollectionUtils.newArrayList(); 
        // log.error("size of ids: " + ids.size());
        eq.fetchAndCreate(ids, ArtObject.baseConstituentsQuery, new ArtObjectConstituent(this), list);

        // create a hashmap of all the art object constituent data indexed by objectid
        // and construct separate lists as we move forward
        Map<Long, List<ArtObjectConstituent>> m = CollectionUtils.newHashMap(); //new HashMap<Long, List<ArtObjectConstituent>>();
        for (ArtObjectConstituent oc : list) {
            List<ArtObjectConstituent> l = m.get(oc.getObjectID());
            if (l == null) {
                l = CollectionUtils.newArrayList();
                m.put(oc.getObjectID(), l);
            }
            l.add(oc);
        }

        // now, sort each list and then register it with the object
        for (Long oid : m.keySet()) {
            ArtObject o = fetchByObjectID(oid);
            List<ArtObjectConstituent> l = m.get(oid);
            Collections.sort(l,ArtObjectConstituent.sortByDisplayOrderAsc);
            o.setConstituents(l);
        }
    }
     */
    
    private SuggestionManager suggestMgr = null;
    private SuggestionManager getSuggestionManager() {
    	return suggestMgr;
    }
    
    private void setSuggestionManager(final SuggestionManager manager) {
    	suggestMgr = manager;
    }

    
    synchronized private void parseArtObjectTitleWords(final Map<Long, ArtObject> newArtObjects,
    						final SuggestionManager suggMgr) {

        if (newArtObjects != null) {
        	suggMgr.setTypeMode(SuggestType.ARTOBJECT_TITLE);
            for (ArtObject o : newArtObjects.values()) {
                if (StringUtils.isNotBlank(o.getTitle())) {
                	Suggest.consumeIndexPair(suggMgr.getMap(SuggestType.ARTOBJECT_TITLE), o.getTitle().toLowerCase(), o, suggMgr);
                }
            }
        }
    }

    synchronized private void parseConstituents(final Map<Long, Constituent> newConstituents, final SuggestionManager suggMgr) {
    	if (newConstituents != null) {
            for (Constituent c : newConstituents.values()) {
            	SuggestType typeTitle = null;
                if (c.isArtistOfNGAObject()) {
                	typeTitle = SuggestType.ARTIST_TITLE;
                }
                else {
                	typeTitle = SuggestType.PROVENANCE_TITLE;
                }
                suggMgr.setTypeMode(typeTitle);
				Suggest.consumeIndexPair(suggMgr.getMap(typeTitle), c.getPreferredDisplayName().toLowerCase(), c, suggMgr);
				if (c.getAltNames() != null) 
                {
                    for (ConstituentAltName a: c.getAltNames()) 
                    {
                        Suggest.consumeIndexPair(suggMgr.getMap(typeTitle), a.getDisplayName(), c, suggMgr);
                    }
                }
                	
            }
            log.info("Done with Constituen Names");
        }
    }

    synchronized private void parseExhibitions(final Map<Long, Exhibition> newExhibitions, final SuggestionManager suggMgr) {
    	if (newExhibitions != null) {
    		suggMgr.setTypeMode(SuggestType.EXHIBITION_TITLE);
            for (Exhibition c : newExhibitions.values()) {
            	Suggest.consumeIndexPair(suggMgr.getMap(SuggestType.EXHIBITION_TITLE), c.getTitle(), c, suggMgr);
            }
            log.info("Done with parseExhibitions");
        }
    }
    
    @Override
    public List<ArtDataSuggestion> suggestArtObjectsByArtistName(final String artistName, final String titleTerm) {
    	// we need to do two things here
        // 1. match on the full string that was entered for list #1
        // 2. match on each string individually and find the intersection
        // of all lists, remove any items from #2 that intersect with #1
        // 3. sort list 1 and list 2 separately then concatenate them together
        
        // match against the full string that was supplied
        Set<ArtDataSuggestion> matches = Suggest.suggest(getSuggestionManager().getMap(SuggestType.ARTOBJECT_TITLE), titleTerm);
        
        String splitWords = titleTerm;
        Set<ArtDataSuggestion> wordMatches = null;
        if (splitWords != null && splitWords.length() > 0) {
            // now add the results from each word separately
            // but only keep results that match ALL words individually 
            for (String s : splitWords.split("\\s+")) {
                Set<ArtDataSuggestion> set = Suggest.suggest(getSuggestionManager().getMap(SuggestType.ARTOBJECT_TITLE), s);
                if (wordMatches == null)
                    wordMatches = set;
                else {
                    // only keep suggestion if it exists for all words supplied
                    Set<ArtDataSuggestion> copyMatches = CollectionUtils.newHashSet();
                    for (ArtDataSuggestion sug : wordMatches) {
                        if (set.contains(sug))
                            copyMatches.add(sug);
                    }
                    wordMatches = copyMatches;
                }
            }
        }
        
        // add results from word matching to any existing results we might have
        if (wordMatches != null && matches != null)
            matches.addAll(wordMatches);
        
        Set<Long> worksIDs = null;
        if (artistName != null && artistName.length() > 0) 
        {
            for (ArtDataSuggestion artistSug : Suggest.suggestNameSet(artistName, getSuggestionManager().getMap(SuggestType.ARTIST_TITLE)) ) 
            {
                if (worksIDs == null)
                {
                    worksIDs = CollectionUtils.newHashSet();
                }
                //log.info("artist: " + artistSug.entityID);
                Constituent c = fetchByConstituentID(artistSug.getEntityID());
                if (c != null)
                {
	                worksIDs.addAll(c.getWorksIDs());
                }
            }
        }
        if (worksIDs != null) 
        {
            // only keep suggestions that also match the suggestions 
            // for the given artist name
            Set<ArtDataSuggestion> set = CollectionUtils.newHashSet();
            if (matches != null) 
            {
                for (ArtDataSuggestion oSug : matches) 
                {
                    //log.info("title:" + oSug.string + " id:" + oSug.entityID);
                    if (worksIDs.contains(Long.valueOf(oSug.getEntityID())))
                        set.add(oSug);
                }
                matches = set;
            }
        }

        // now we need to remove duplicates and sort w.r.t. the given
        // string
        Set<ArtDataSuggestion> primaryTitles = CollectionUtils.newHashSet();
        Set<ArtDataSuggestion> secondaryTitles = CollectionUtils.newHashSet();
        String title = StringUtils.removeDiacritics(StringUtils.cleanupForMatching(titleTerm));
        if (matches != null) {
            for (ArtDataSuggestion suggestion : matches) 
            {
                String sugg = StringUtils.removeDiacritics(StringUtils.cleanupForMatching(suggestion.getCompareString()));
                if (sugg.startsWith(title))
                    primaryTitles.add(suggestion);
                else
                    secondaryTitles.add(suggestion);
            }
        }
            
        // remove any duplicates
        for (ArtDataSuggestion s : primaryTitles) {
            if (secondaryTitles.contains(s))
                secondaryTitles.remove(s);
        }
        
        // sort the two lists independently and then combine them  
        List<ArtDataSuggestion> suggestions = CollectionUtils.toSortedAlphaDiacriticNormalizedList(primaryTitles);
        suggestions.addAll(CollectionUtils.toSortedAlphaDiacriticNormalizedList(secondaryTitles));
        
        return suggestions;
    }

    @Override
    public List<ArtDataSuggestion> suggestArtObjectsByTitle(final String baseName) {
    	return Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, getSuggestionManager().getMap(SuggestType.ARTOBJECT_TITLE)));
    }

    @Override
    public List<ArtDataSuggestion> suggestArtists(final String baseName) {
    	return Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, getSuggestionManager().getMap(SuggestType.ARTIST_TITLE)));
    }

    @Override
    public List<ArtDataSuggestion> suggestExhibitions(final String baseName) {
    	return Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, getSuggestionManager().getMap(SuggestType.EXHIBITION_TITLE)));
    }
    
    @Override
    public List<ArtDataSuggestion> suggestOwners(final String baseName) {
        return Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, getSuggestionManager().getMap(SuggestType.PROVENANCE_TITLE)));
    }
    
    synchronized private void setExhibitions(final Map<Long, Exhibition> newExhibitions) {
    	exhibitions = newExhibitions;
    }
    
    public Map<Long, Exhibition> getExhibtionsRaw() {
    	return exhibitions != null ? exhibitions : CollectionUtils.newHashMap();
    }
    
    public List<Exhibition> getExhibtions() {
    	final List<Exhibition> list = CollectionUtils.newArrayList();
    	if (exhibitions != null)
    	{
    		list.addAll(exhibitions.values());
    	}
    	return list;
    }
    
    @Override
    public Exhibition fetchByExhibtionID (final long id) throws DataNotReadyException {
    	Exhibition result = null;
    	if (exhibitions != null) result = exhibitions.get(id);
    	return result;
    }
    
    @Override
	public List<Exhibition>	fetchByExhibitionIDS(final List<Long> ids) throws DataNotReadyException {
    	final List<Exhibition> results = CollectionUtils.newArrayList();
    	if (exhibitions != null && ids != null)
    	{
    		for (Long id: ids) {
    			if (id != null)
    			{
    				Exhibition cnd = exhibitions.get(id);
    				if (cnd != null)
    				{
    					results.add(cnd);
    				}
    			}
    		}
    	}
    	return results;
    }

    synchronized private void setArtObjects(Map<Long, ArtObject> newArtObjects) {
        artObjects = newArtObjects;
    }

    synchronized private void setArtObjectTitleWords(Map<String, Set<Suggestion>> newArtObjectTitleWords) {
        artObjectTitleWords = newArtObjectTitleWords;
    }

    
    synchronized private void setStandardArtObjectFacets(List<Facet> standardArtObjectFacets) {
        this.standardArtObjectFacets = standardArtObjectFacets;
    }

    synchronized private void setAllIndexOfArtistRanges(Map<String, String> allIndexOfArtistRanges) {
        this.allIndexOfArtistRanges = allIndexOfArtistRanges;
    }

    public List<ArtObject> getArtObjects() {
    	return CollectionUtils.newArrayList(getArtObjectsRaw().values());
    }
    
    public Map<Long, ArtObject> getArtObjectsRaw() {
        return artObjects;
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
            searchArtObjects(sh,null,fn);
            setStandardArtObjectFacets(fn.getFacets()); 
        }
        return standardArtObjectFacets;
    }
    
    // DPB - I actually don't think we need this
    // separate the images into a map indexed by object id
/*    private Map<String, Derivative> derivativesByImageID = CollectionUtils.newHashMap();
    public Derivative fetchDerivativeByImageID(String imageID) {
    	return derivativesByImageID.get(imageID);
    }
    synchronized private void clearDerivativesByImageID() {
    	derivativesByImageID = CollectionUtils.newHashMap();
    }
*/
    
    synchronized protected <T extends Derivative> void loadImagery(Map<Long, ArtObject> newArtObjects, T seed) throws SQLException {
        
        EntityQuery<T> deq = new EntityQuery<T>(getDataSourceService());
        log.info("Starting pre-fetch of all " + seed.getClass().getName() + " images");
        List<T> newImages = deq.fetchAll(seed.getAllImagesQuery(), seed);
        log.info("found this many " + seed.getClass().getName() + " images: " + newImages.size());
        
        // add to the derivatives list
//        derivativesRaw.addAll(newImages);
        
        // separate the images into a map indexed by object id
        Map<Long, List<T>> imgByObject = CollectionUtils.newHashMap();
        for (T d : newImages) {
        	//  DPB - I don't think we need this actually 
        	//  derivativesByImageID.put(d.getImageID(), d);
            ArtObject o = (ArtObject)newArtObjects.get(d.getArtObjectID());
            if ( o != null && o.imageOK(d) ) {
                List<T> ld = imgByObject.get(d.getArtObjectID());
                if (ld == null) {
                    ld = CollectionUtils.newArrayList();
                    imgByObject.put(d.getArtObjectID(), ld);
                }
                ld.add(d);
            }
        }

        // then assign those lists of images to each object
        for (Long id : imgByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<T> l = imgByObject.get(id);
            if (o != null && l != null) {
                o.setImages(l, seed);
            }
        } 

    }

    // load all of the art object data into our cached map
    synchronized protected Map<Long, ArtObject> getArtObjects(
            List<ArtObjectConstituent> ocs,
            List<ArtObjectTextEntry> textEntries,
            List<ArtObjectHistoricalData> aohist,
            List<ArtObjectDimension> aoDims,
            List<ArtObjectAssociationRecord> aoas,
            List<ArtObjectComponent> aocomps
    ) throws SQLException {

        // ART OBJECT ASSOCIATIONS 
        // load the art object associations into two maps indexed by the art object ID and containing a list of ArtObjectAssocations  
        /*Map<Long, List<ArtObjectAssociation>> childAssociations = CollectionUtils.newHashMap();
        Map<Long, List<ArtObjectAssociation>> parentAssociations = CollectionUtils.newHashMap();
        for (ArtObjectAssociation aoa : aoas) {
            List<ArtObjectAssociation> children = childAssociations.get(aoa.getParentObjectID());
            if (children == null) {
                children = CollectionUtils.newArrayList();
                childAssociations.put(aoa.getParentObjectID(), children);
            } 
            children.add(aoa);

            List<ArtObjectAssociation> parents = parentAssociations.get(aoa.getChildObjectID());
            if (parents == null) {
                parents = CollectionUtils.newArrayList();
                parentAssociations.put(aoa.getChildObjectID(), parents);
            } 
            parents.add(aoa);
        }*/

        Map<Long, List<ArtObjectAssociationRecord>> associations = CollectionUtils.newHashMap();
        for (ArtObjectAssociationRecord aoa : aoas) {
            List<ArtObjectAssociationRecord> objAssociations = associations.get(aoa.getParentObjectID());
            if (objAssociations == null) {
                objAssociations = CollectionUtils.newArrayList();
                associations.put(aoa.getParentObjectID(), objAssociations);
            } 
            objAssociations.add(aoa);

            objAssociations = associations.get(aoa.getChildObjectID());
            if (objAssociations == null) {
                objAssociations = CollectionUtils.newArrayList();
                associations.put(aoa.getChildObjectID(), objAssociations);
            } 
            objAssociations.add(aoa);
        }

        // OBJECTS THEMSELVES
        Map<Long, ArtObject> newArtObjects = CollectionUtils.newHashMap();
        EntityQuery<ArtObject> eq = new EntityQuery<ArtObject>(getDataSourceService());
        log.info("Starting pre-fetch of all objects");
        ArtObject.setFetchAllObjectsQuery(getOperatingMode());
        List<ArtObject> newObjects = eq.fetchAll(ArtObject.fetchAllObjectsQuery, new ArtObject(this));
        log.info("found this many objects: " + newObjects.size());
        for (ArtObject o : newObjects) {
            // create blank lists for all objects by default so that
            // we don't try to load them again later if they're actually blank
            o.setConstituents();
            o.setTerms();
            o.setImages();
            o.setAssociations(associations.get(o.getObjectID()));
            newArtObjects.put(o.getObjectID(), o);
        }

        // CONSTITUENT RELATIONSHIPS
        // load the constituents into a huge map of lists indexed by object id
        Map<Long, List<ArtObjectConstituent>> mapByObject = CollectionUtils.newHashMap(); //new HashMap<Long, List<ArtObjectConstituent>>();
        for (ArtObjectConstituent oc : ocs) {
            List<ArtObjectConstituent> loc = mapByObject.get(oc.getObjectID());
            if (loc == null) {
                loc = CollectionUtils.newArrayList();
                mapByObject.put(oc.getObjectID(), loc);
            }
            loc.add(oc);
        }

        // set the list of constituents for each art object
        // pre-sorting the list by the displayOrder contained in the relationship
        for (Long id : mapByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<ArtObjectConstituent> l = mapByObject.get(id);
            if (o != null && l != null) {
                Collections.sort(l,ArtObjectConstituent.sortByDisplayOrderAsc);
                o.setConstituents(l);
            }
        }

        // OBJECT IMAGES
        loadImagery(newArtObjects, new ArtObjectImage(this));
        loadImagery(newArtObjects, new ResearchImage(this));
        
        checkImageSizes(newArtObjects);
        
        // OBJECT TERMS
        EntityQuery<ArtObjectTerm> teq = new EntityQuery<ArtObjectTerm>(getDataSourceService());
        log.info("Starting pre-fetch of all object terms");
        List<ArtObjectTerm> newTerms = teq.fetchAll(ArtObjectTerm.fetchAllObjectTermsQuery, new ArtObjectTerm(this));
        log.info("found this many object terms: " + newTerms.size());

        // separate the terms into a map indexed by object id
        // and store a list of object IDs for each term for fast access
        Map<Long, List<ArtObjectTerm>> termsByObject = CollectionUtils.newHashMap();//new HashMap<Long, List<ArtObjectTerm>>();
        for (ArtObjectTerm t : newTerms) {
            ArtObject o = newArtObjects.get(t.getObjectID());
            if ( o != null ) {
                List<ArtObjectTerm> lt = termsByObject.get(t.getObjectID());
                if (lt == null) {
                    lt = CollectionUtils.newArrayList();
                    termsByObject.put(t.getObjectID(), lt);
                }
                lt.add(t);
            }
        }

        // then assign those lists of terms to each object
        for (Long id : termsByObject.keySet()) {
            ArtObject o = (ArtObject)newArtObjects.get(id);
            List<ArtObjectTerm> l = termsByObject.get(id);
            if (o != null && l != null) {
                o.setTerms(l);
            }
        }
        
        log.info("Assigning text entries to art objects");
        for (ArtObjectTextEntry te : textEntries) {
            ArtObject o = (ArtObject)newArtObjects.get(te.getObjectID());
            if (o != null)
                o.addTextEntry(te);
        }

        log.info("Assigning historical data entries to art objects");
        for (ArtObjectHistoricalData h : aohist) {
            ArtObject o = (ArtObject)newArtObjects.get(h.getObjectID());
            if (o != null)
                o.addHistoricalData(h);
        }
        
        log.info("Assigning dimensions to art objects");
        for (ArtObjectDimension d : aoDims) {
            ArtObject o = (ArtObject)newArtObjects.get(d.getObjectID());
            if (o != null)
                o.addDimensions(d);
        }
        
        log.info("Assigning components to art objects");
        for (ArtObjectComponent c : aocomps) {
            ArtObject o = (ArtObject)newArtObjects.get(c.getObjectID());
            if (o != null)
                o.addComponent(c);
        }

        return newArtObjects;

    }

    public <C extends Constituent>C fetchByConstituentID(long cID, ConstituentFactory<C> factory) {
        return factory.createConstituent(getConstituentsRaw().get(cID));
    }

    public Constituent fetchByConstituentID(long cID) {
        return fetchByConstituentID(cID, constFactory);
    }

    public <C extends Constituent>List<C> fetchByConstituentIDs(Collection<Long> objectIDs, ConstituentFactory<C> factory) {
        List<C> have = CollectionUtils.newArrayList(); 

        if (objectIDs != null) {
            for (Long id : objectIDs) {
                C o = factory.createConstituent(getConstituentsRaw().get(id));
                have.add(o);
            }
        }

        return have;
    }

    public List<Constituent> fetchByConstituentIDs(Collection<Long> objectIDs) {
        return fetchByConstituentIDs(objectIDs, constFactory);
    }

    public <C extends Constituent>List<C> fetchByConstituentIDs(Collection<Long> objectIDs, ConstituentFactory<C> factory, gov.nga.entities.art.Constituent.SORT... order) {
        List<C> list = (List<C>) fetchByConstituentIDs(objectIDs, factory);

        // sort the list of objects before returning it
        SortHelper<C> sh = new SortHelper<C>();
        sh.sortArtEntities(list, (Object[]) order);

        return list;
    }

    public List<Constituent> fetchByConstituentIDs(Collection<Long> objectIDs, gov.nga.entities.art.Constituent.SORT... order) {
        return fetchByConstituentIDs(objectIDs, constFactory, order);
    }

    public <C extends Constituent>List<C> searchConstituents(SearchHelper<C> sh, ResultsPaginator pn, FacetHelper fn, SortHelper<C> sortH, ConstituentFactory<C> factory, FreeTextSearchable<C> freeTextSearcher) {
        List<C> list = new ArrayList<C>();
        for (Constituent constObj: getConstituentsRaw().values())
        {
            list.add(factory.createConstituent(constObj));
        }
        sh.setFreeTextServicer(freeTextSearcher);
        return sh.search(list, pn, fn, sortH);
    }

    //public <E extends ArtEntity> List<E> searchArtEntity(List<E> list, SearchHelper<E> sh, ResultsPaginator pn, FacetHelper fn, SortHelper<E> sortH) {
    //    return sh.search(list, pn, fn, sortH);
   // }

 /* 
  * private List<Derivative> derivativesRaw = CollectionUtils.newArrayList();(non-Javadoc)
    public List<Derivative> getDerivatives() {
    	return CollectionUtils.newArrayList(this.derivativesRaw);
    }
    synchronized private void clearDerivativesRaw() {
    	derivativesRaw = CollectionUtils.newArrayList();
    }
 */
    
    public <C extends Constituent>List<C> searchConstituents(SearchHelper<C> sh, ResultsPaginator pn, FacetHelper fn, SortHelper<C> sortH, ConstituentFactory<C> factory) {
        return searchConstituents(sh, pn, fn, sortH, factory, new ArtEntityFreeTextSearch<C>());
    }

    public List<Constituent> searchConstituents(SearchHelper<Constituent> searchH, ResultsPaginator pn, FacetHelper fn, SortHelper<Constituent> sortH) {
        return searchConstituents(searchH, pn, fn, sortH, constFactory);
    }
    
    public <C extends Constituent>List<C> searchConstituents(SearchHelper<C> searchH, ResultsPaginator pn, FacetHelper fn, ConstituentFactory<C> factory, FreeTextSearchable<C> freeTextSearcher, Object... order) {
        SortHelper<C> sortH = null;
        if (order != null && order.length > 0) {
            sortH = new SortHelper<C>();
            sortH.setSortOrder(order);
        }
        return searchConstituents(searchH, pn, fn, sortH, factory, freeTextSearcher);
    }
    
    public <C extends Constituent>List<C> searchConstituents(SearchHelper<C> searchH, ResultsPaginator pn, FacetHelper fn, ConstituentFactory<C> factory, Object... order) {
        
        return searchConstituents(searchH, pn, fn, factory,  new ArtEntityFreeTextSearch<C>(), order);
    }

    public List<Constituent> searchConstituents(SearchHelper<Constituent> searchH, ResultsPaginator pn, FacetHelper fn, Object... order) {
        
        return searchConstituents(searchH, pn, fn, constFactory, order);
    }
    
    synchronized private void setConstituents(Map<Long, Constituent> newConstituents) {
        constituents = newConstituents;
    }

    synchronized private void setArtistAltNames(Map<String, Set<Suggestion>> newArtistAltNames) {
        artistAltNames = newArtistAltNames;
    }

    synchronized private void setOwnerAltNames(Map<String, Set<Suggestion>> newOwnerAltNames) {
        ownerAltNames = newOwnerAltNames;
    }
    
    public Map<Long, Constituent> getConstituentsRaw() {
        return constituents;
    }

    // load all constituent data
    synchronized protected Map<Long, Constituent> getConstituents(List<ArtObjectConstituent> ocs, List<ConstituentAltName> alts, List<ConstituentTextEntry> ctes) throws SQLException {

        Map<Long, Constituent> newConstituents = CollectionUtils.newHashMap();

        EntityQuery<Constituent> eq = new EntityQuery<Constituent>(getDataSourceService());
        log.info("Starting pre-fetch of all constituents");
        List<Constituent> list = eq.fetchAll(Constituent.fetchAllConstituentsQuery, new Constituent(this));
        log.info("found this many constituents: " + list.size());

        // store constituents in a map, indexed by constituent ID
        for (Constituent c : list ) {
            newConstituents.put(c.getConstituentID(), c);
        }

        // distribute the constituent alt names to the constituents
        log.info("Assigning all alternate names to constituents");
        for (ConstituentAltName alt : alts) {
            Constituent c = newConstituents.get(alt.getConstituentID());
            if (c != null) {
                c.addAltName(alt);
            }
            else {
                log.error("Could not locate constituent " + alt.getConstituentID() + " to place alternate name data");
            }
        }
        
        log.info("Assigning Constituent Bibliography Entries");
        for (ConstituentTextEntry e : ctes) {
            Constituent c = newConstituents.get(e.getConstituentID());
            if (c != null)
                c.addTextEntry(e);
        }
        
        // load the constituents into a huge map of lists indexed by object id
        // and into another huge map indexed by constituent ids
        log.info("Assigning all object roles to all constituents");
        Map<Long, List<ArtObjectConstituent>> mapByConstituent = CollectionUtils.newHashMap(); //new HashMap<Long, List<ArtObjectConstituent>>();
        for (ArtObjectConstituent oc : ocs) {
            List<ArtObjectConstituent> loc = mapByConstituent.get(oc.getConstituentID());
            if (loc == null) {
                loc = CollectionUtils.newArrayList();
                mapByConstituent.put(oc.getConstituentID(), loc);
            }
            // add this object constituent relationship to the list of relationships for its constituent
            // sometimes, an artist can be listed with multiple roles such as both an artist and a related artist
            // and we must store all of them - individual methods such as getWorks() should remove any duplicated works
            // or duplicate artists as necessary (and if necessary)
            loc.add(oc);
        }

        // set the list of art object relationships for each constituent
        // with no pre-defined sort order
        for (Long id : mapByConstituent.keySet()) {
            Constituent c = newConstituents.get(id);
            List<ArtObjectConstituent> loc = mapByConstituent.get(id);
            if (c != null && loc != null) {
                c.setObjectRoles(loc);
            }
        }

        return newConstituents;
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
            searchConstituents(sh,null,fn);
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

    public Location fetchByLocationID(long locationID) {
        return getLocationsRaw().get(locationID);   
        // currently, the TMS extract does not null out the location ID
        // of objects if that location is not public.  Until we do that
        // we cannot contingently expect to fetch the location when
        // it turns out to be null here because we may never get one
        // if the location isn't a public location
    }

    public Place fetchByPlaceKey(String locationKey) {
        return getPlacesRaw().get(locationKey);   
        // currently, the TMS extract does not null out the location ID
        // of objects if that location is not public.  Until we do that
        // we cannot contingently expect to fetch the location when
        // it turns out to be null here because we may never get one
        // if the location isn't a public location
    }

    public Place fetchByTMSLocationID(long tmsLocationID) {
        return getPlacesTMSLocationsRaw().get(tmsLocationID);   
        // currently, the TMS extract does not null out the location ID
        // of objects if that location is not public.  Until we do that
        // we cannot contingently expect to fetch the location when
        // it turns out to be null here because we may never get one
        // if the location isn't a public location
    }

    public List<Location> fetchByLocationIDs(List<Long> locationIDs) {
        //      List<Long> need = CollectionUtils.newArrayList();
        List<Location> have = CollectionUtils.newArrayList(); 

        if (locationIDs != null) {
            for (Long l : locationIDs) {
                Location ol = getLocationsRaw().get(l);
                //              if (ol == null)
                //                  need.add(l);
                //              else
                have.add(ol);

            }
        }
        /*      if (need.size() > 0) {
            try {
                EntityQuery<Location> eq = new EntityQuery<Location>(getPoolService());
                List<Location> newLocations = CollectionUtils.newArrayList();
                eq.fetchAndCreate(need, Location.briefLocationsQuery, new Location(this), newLocations);
                for (Location ol : newLocations) {
                    synchronized (getLocationsRaw()) {
                        getLocationsRaw().put(ol.getLocationID(), ol);
                    }
                    have.add(ol);
                }
            }
            catch (SQLException se) {
                // not much to do here - if it fails, it fails
                // but hopefully we've already loaded all of these into memory anyway
            }
        }
         */     
        return have;
    }

    synchronized private void setLocations(Map<Long, Location> newLocations) {
        locations = newLocations;
        rooms = null;
    }

    public Map<Long, Location> getLocationsRaw() {
        return locations;
    }

    synchronized private void setPlaces(Map<String, Place> newPlaces) {
        places = newPlaces;
    }

    public Map<String, Place> getPlacesRaw() {
        return places;
    }

    synchronized private void setPlacesTMSLocations(Map<Long, Place> newPlacesTMSLocations) {
        placesTMSLocations = newPlacesTMSLocations;
        for (Long tmsid : placesTMSLocations.keySet()) {
        	Place p = placesTMSLocations.get(tmsid);
        	p.addTMSLocationID(tmsid);
        }
    }

    public Map<Long, Place> getPlacesTMSLocationsRaw() {
        return placesTMSLocations;
    }

    synchronized private void setMediaItems(Map<Long, Media> newMediaItems) {
        this.mediaItems = newMediaItems;
    }

    public Map<Long, Media> getMediaItemsRaw() {
        return mediaItems;
    }

    public Map<String, List<Media>> getMediaRelationshipsRaw() {
        return mediaRelationships;
    }

    synchronized private void setMediaRelationships(Map<String, List<Media>> newMediaRelationships) {
        this.mediaRelationships = newMediaRelationships;
    }

    public Media fetchByMediaID(Long mediaID) {
        return getMediaItemsRaw().get(mediaID);   
    }
    
    public List<Media> getMediaByEntityRelationship(String entityUniqueID) {
        return getMediaRelationshipsRaw().get(entityUniqueID);   
    }


/*  public Map<Long, Location> getLocations() {
        return CollectionUtils.newHashMap(getLocationsRaw());
    }
*/
    
    private Map<String, String> rooms = null;
    public Map<String, String> getAllLocationDescriptionsByRoom() {
        if (rooms == null) {
            Map<String, String> newrooms = CollectionUtils.newHashMap();
            Map<Long, Location> locs = getLocationsRaw();
            if (locs != null) {
                for (Location l : locs.values()) {
                    newrooms.put(l.getRoom(), l.getDescription());
                }
            }
            synchronized (this) {
                rooms = newrooms;
            }
        }
        return rooms;
    }

    /*
    // load all art object locations
    synchronized protected Map<Long, Location> loadLocations() throws SQLException {
        Map<Long, Location> newLocations = CollectionUtils.newHashMap();
        EntityQuery<Location> eq = new EntityQuery<Location>(getDataSourceService());
        log.info("Starting pre-fetch of all locations");
        List<Location> newObjectLocations = eq.fetchAll(Location.fetchAllLocationsQuery, new Location(this));
        log.info("found this many locations: " + newObjectLocations.size());
        for (Location l : newObjectLocations) {
            newLocations.put(l.getLocationID(), l);
        }
        return newLocations;
    }
    */

    // load all web defined places which (mostly) have TMS objects residing in them
    synchronized protected Map<String, Place> loadPlaces() throws SQLException {
        Map<String, Place> newPlacesMap = CollectionUtils.newHashMap();
        EntityQuery<Place> eq = new EntityQuery<Place>(getDataSourceService());
        log.info("Starting pre-fetch of all place definitions");
        List<Place> newPlacesList= eq.fetchAll(Place.fetchAllPlacesQuery, new Place(this));
        log.info("found this many place definitions: " + newPlacesList.size());
        for (Place l : newPlacesList) {
            newPlacesMap.put(l.getPlaceKey(), l);
        }
        return newPlacesMap;
    }

    // load all web defined place to tms object location associations
    synchronized protected Map<Long, Place> loadPlacesTMSLocations(Map<String, Place> places) throws SQLException {
        Map<Long, Place> newPlaceTMSLocations = CollectionUtils.newHashMap();
        EntityQuery<PlaceRelationships> eq = new EntityQuery<PlaceRelationships>(getDataSourceService());
        log.info("Starting pre-fetch of all place to TMS Location relationships");
        List<PlaceRelationships> newLocations = eq.fetchAll(PlaceRelationships.fetchAllPlaceTMSLocationsQuery, new PlaceRelationships(this));
        log.info("found this many place to tms location relationships: " + newLocations.size());
        for (PlaceRelationships l : newLocations) {
            newPlaceTMSLocations.put(l.getTMSLocationID(), places.get(l.getPlaceKey()));
        }
        return newPlaceTMSLocations;
    }
    
    // load all web defined places which (mostly) have TMS objects residing in them
    synchronized protected Map<Long, Media> loadMediaItems() throws SQLException {
        Map<Long, Media> newMediaMap = CollectionUtils.newHashMap();
        EntityQuery<Media> eq = new EntityQuery<Media>(getDataSourceService());
        log.info("Starting pre-fetch of all media definitions");
        List<Media> newMediaList = eq.fetchAll(Media.fetchAllMediaQuery, new Media(this));
        log.info("found this many place definitions: " + newMediaList.size());
        for (Media m : newMediaList) {
            newMediaMap.put(m.getMediaID(), m);
        }
        return newMediaMap;
    }

    // load all web defined place to tms object location associations
    synchronized protected Map<String, List<Media>> loadMediaRelationships(Map<Long, Media> mediaItems) throws SQLException {
        Map<String, List<Media>> newRelationshipsMap = CollectionUtils.newHashMap();
        EntityQuery<MediaRelationship> eq = new EntityQuery<MediaRelationship>(getDataSourceService());
        log.info("Starting pre-fetch of all Media relationships");
        List<MediaRelationship> newRelationshipsList = eq.fetchAll(MediaRelationship.fetchAllMediaRelationshipsQuery, new MediaRelationship(this));
        log.info("found this many Media relationships: " + newRelationshipsList.size());
        for (MediaRelationship mr : newRelationshipsList) {
        	List<Media> l = newRelationshipsMap.get(mr.getEntityUniqueID());
        	if ( l == null ) {
        		l = CollectionUtils.newArrayList();
        		newRelationshipsMap.put(mr.getEntityUniqueID(), l);
        	}
        	l.add(mediaItems.get(mr.getMediaID()));
        }
        return newRelationshipsMap;
    }


    // load all art object components
    synchronized protected List<ArtObjectComponent> loadComponents() throws SQLException {
        EntityQuery<ArtObjectComponent> eq = new EntityQuery<ArtObjectComponent>(getDataSourceService());
        log.info("Starting pre-fetch of all components");
        List<ArtObjectComponent> newComponents = eq.fetchAll(ArtObjectComponent.fetchAllComponentsQuery, new ArtObjectComponent(this));
        log.info("found this many components: " + newComponents.size());
        return newComponents;
    }

    private void checkImageSizes(final Map<Long, ArtObject> newArtObjects)
    {
        try
        {
            log.info("Starting checking zoom image sizes");
            for (ArtObject ao : newArtObjects.values())
            {
                List<ResearchImage> techImages = new ArrayList<ResearchImage>();
                Set<String> seqs = new HashSet<String>();
                for (ResearchImage image : ao.getResearchImages())
                {
                    if (Derivative.IMGFORMAT.PTIF.equals(image.getFormat())
                            && Derivative.IMGVIEWTYPE.TECHNICAL.equals(image.getViewType())
                            && image.getAltImageRef() == null
                            && image.getIsZoomable())
                    {
                        techImages.add(image);
                        seqs.add(org.apache.commons.lang3.StringUtils.substringBefore(image.getSequence(), "."));
                    }
                }

                nextSequence:
                for (String seq : seqs)
                {
                    List<ResearchImage> sameSeq = findBySequenceNumber(techImages, seq);
                    if (sameSeq.size() > 1)
                    {
                        Long width = sameSeq.get(0).getWidth();
                        Long height = sameSeq.get(0).getHeight();
                        for (ResearchImage im : sameSeq)
                        {
                            if (!width.equals(im.getWidth()) || !height.equals(im.getHeight()))
                            {
                                log.error("Some images have different sizes for ObjID={} and sequence={} ", im.getArtObjectID(), seq);
                                break nextSequence;
                            }
                        }
                    }
                }
            }
            log.info("Completed checking zoom image sizes");
        } catch (Exception e)
        {
            log.error("Error in checkImageSizes", e);
        }
    }

    private List<ResearchImage> findBySequenceNumber(List<ResearchImage> allImages, String sequence)
    {
        List<ResearchImage> result = new ArrayList<ResearchImage>();
        for (ResearchImage image : allImages)
        {
            if (sequence.equals(org.apache.commons.lang3.StringUtils.substringBefore(image.getSequence(), ".")))
                result.add(image);
        }
        return result;
    }


}
