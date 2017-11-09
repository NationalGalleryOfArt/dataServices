package gov.nga.entities.art;

import gov.nga.entities.art.ArtEntity.OperatingMode;
import gov.nga.entities.art.ArtObject.FACET;
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
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.ConfigService;
import gov.nga.utils.MutableInt;
import gov.nga.utils.StringUtils;
import gov.nga.utils.SystemUtils;
import gov.nga.utils.TypeUtils;
import gov.nga.utils.db.DataSourceService;

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
public class ArtDataManager extends MessageProviderImpl implements Runnable, ArtDataManagerService { 

// TODO - make the TMS data manager class configurable to run either in private or public exclusively via artdatamanager configuration settings
// TODO - separate all JCR related calls from the base ArtDataManager to keep it pure and simple and create new art data manager CQ implementation wrapper for OSGI
// TODO - add a TMS basics JAR dependency in CQ that is generated from my separate GIT repository and simply provided to the AEM team

    private static final int CONTENT_SYNC_DELAY = 15;
//	private static final String TMS_BUNDLE_REFRESH = "tmsBundleRefresh";
//	private static final String TMS_ART_DATA_JCR_SYNC = "tmsArtDataJCRSync";

	private static ArtObjectFactory<ArtObject> artObjFactory = new ArtObjectFactoryImpl();
    
    private static ConstituentFactory<Constituent> constFactory = new ConstituentFactoryImpl();
    
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
    	if ( !StringUtils.isNullOrEmpty(opmode) && opmode.equals(OperatingMode.PRIVATE.toString()) )
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
        log.info("**************************** Unloading Previous Art Data Manager cached data *************************************************");
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
        System.gc();
        log.info(SystemUtils.freeMemorySummary());
//        clearDerivativesRaw();
    }

    synchronized public boolean load() {

    	log.info("**************************** Starting Load of Refreshed Art Data Manager Cached Data Set *************************************");

        // load all art object constituent data first, then pass that
        // map to both the object manager and constituent manager
        try {
            log.info("Loading all object constituent relationships");
            EntityQuery<ArtObjectConstituent> eq = new EntityQuery<ArtObjectConstituent>(getDataSourceService());
            List<ArtObjectConstituent> ocs = eq.fetchAll(ArtObjectConstituent.fetchAllObjectsConstituentsQuery, new ArtObjectConstituent(this));
            log.info("found this many object constituent relationships: " + ocs.size());

            log.info("Loading all location and related data");
            Map<Long, Location> newLocations = loadLocations();
            
            log.info("Loading all components");
            List<ArtObjectComponent> aocomps = loadComponents();
            
            log.info("Loading all art object text entries");
            EntityQuery<ArtObjectTextEntry> teq = new EntityQuery<ArtObjectTextEntry>(getDataSourceService());
            List<ArtObjectTextEntry> teList = teq.fetchAll(ArtObjectTextEntry.allTextEntryQuery, new ArtObjectTextEntry(this));
            log.info("found this many art object text entries: " + teList.size());

            log.info("Loading all art object historical data entries");
            EntityQuery<ArtObjectHistoricalData> aohistq = new EntityQuery<ArtObjectHistoricalData>(getDataSourceService());
            List<ArtObjectHistoricalData> aohist = aohistq.fetchAll(ArtObjectHistoricalData.allHistoricalDataQuery, new ArtObjectHistoricalData(this));
            log.info("found this many art object historical data entries: " + aohist.size());

            log.info("Loading all art object dimensions");
            EntityQuery<ArtObjectDimension> aoDimsq = new EntityQuery<ArtObjectDimension>(getDataSourceService());
            List<ArtObjectDimension> aoDims = aoDimsq.fetchAll(ArtObjectDimension.allObjectsDimensionsQuery, new ArtObjectDimension(this));
            log.info("found this many art object dimensions: " + aoDims.size());

            log.info("Loading all inter art object associations");
            EntityQuery<ArtObjectAssociationRecord> aq = new EntityQuery<ArtObjectAssociationRecord>(getDataSourceService());
            List<ArtObjectAssociationRecord> aoas = aq.fetchAll(ArtObjectAssociationRecord.fetchAllArtObjectAssociationsQuery, new ArtObjectAssociationRecord(this));
            log.info("found this many art object assocations: " + aoas.size());

            log.info("Loading all art objects and related data");
            Map<Long, ArtObject> newArtObjects = getArtObjects(ocs, teList, aohist, aoDims, aoas, aocomps);
            
            EntityQuery<ConstituentAltName> ceq = new EntityQuery<ConstituentAltName>(getDataSourceService());
            log.info("Loading all constituent alternate names");
            List<ConstituentAltName> alts = ceq.fetchAll(ConstituentAltName.fetchAllConstituentAltNamessQuery, new ConstituentAltName(this));
            log.info("found this many constituent alternate names: " + alts.size());

            log.info("Loading all constituent text entries");
            EntityQuery<ConstituentTextEntry> cte = new EntityQuery<ConstituentTextEntry>(getDataSourceService());
            List<ConstituentTextEntry> ctes = cte.fetchAll(ConstituentTextEntry.allTextEntryQuery, new ConstituentTextEntry(this));
            log.info("found this many constituent text entries: " + ctes.size());

            log.info("Loading all constituent and related data");
            Map<Long, Constituent> newConstituents = getConstituents(ocs, alts, ctes);
            
            log.info("Computing art object title words and constituent altnames for suggest feature");
            
            Map<String, Set<Suggestion>> newArtObjectTitleWords = parseArtObjectTitleWords(newArtObjects);
        	Map<String, Set<Suggestion>> newArtistAltNames = parseArtistAltNames(newConstituents);
        	Map<String, Set<Suggestion>> newOwnerAltNames = parseOwnerAltNames(newConstituents);

        	// dump any cached data we might still have before setting the new set in place  
        	unload();
        	setLocations(newLocations);
        	setArtObjects(newArtObjects);
        	setArtObjectTitleWords (newArtObjectTitleWords);
        	setConstituents(newConstituents);
        	setArtistAltNames(newArtistAltNames);
        	setOwnerAltNames(newOwnerAltNames);
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

            return true;
        }
        // if we are unable to fetch the data we need, then we reschedule
        // ourselves which will attempt to kick off another refresh immediately
        catch (SQLException se) {
            log.error("ERROR Loading TMS Data: " + se.getMessage(), se );
        }
        return false;
    }

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
    
    private static String cleanupForMatching(String original) {
        if (original == null)
            return null;
        
        return
            StringUtils.removeHTML(original).toLowerCase()
            .replaceAll("^[^\\p{L}]+", " ")
            .replaceAll("[^\\p{L}]+$", " ")
            .replaceAll("\\s+[^\\p{L}]+", " ")
            .replaceAll("[^\\p{L}]+\\s+", " ")
            .replaceAll("-{2,}", " ")
            .replaceAll("[^-\'\\p{L}]", " ")
            // get rid of non-letter characters at beginning of word
            .replaceAll("^[^\\p{L}]+", "")
            // and at end of word
            .replaceAll("[^\\p{L}]+$", "");
        
    }
    
    private static String cleanupForLettersOnlyMatching(String original)
    {
        String cleanedString = null;
        if (original != null)
        {
            cleanedString = cleanupForMatching(original)
                                .replaceAll("[\']+", "")
                                .replaceAll("[-/]+", " ");
        }
        return cleanedString;
    }
    
    synchronized private Map<String, Set<Suggestion>> parseArtObjectTitleWords(Map<Long, ArtObject> newArtObjects) {
    	Map<String, Set<Suggestion>> newArtObjectTitleWords = null;
        newArtObjectTitleWords = CollectionUtils.newTreeMap(
                new Comparator<String>() {
                    public int compare(String a, String b) {
                        return StringUtils.getDefaultCollator().compare(a, b);
                    }
                }
        );

        if (newArtObjects != null) {
            for (ArtObject o : newArtObjects.values()) {
                String title = o.getTitle();
                consumeIndexPair(newArtObjectTitleWords, title, o.getObjectID());
            }
        }
        return newArtObjectTitleWords;
    }

    public List<String> suggestArtObjectTitles(String artistName, String titleWords) {

        isDataReady(true);

        // we need to do two things here
        // 1. match on the full string that was entered for list #1
        // 2. match on each string individually and find the intersection
        // of all lists, remove any items from #2 that intersect with #1
        // 3. sort list 1 and list 2 separately then concatenate them together
        
        // match against the full string that was supplied
        Set<Suggestion> matches = suggest(artObjectTitleWords,titleWords);
        
        String splitWords = titleWords;
        Set<Suggestion> wordMatches = null;
        if (splitWords != null && splitWords.length() > 0) {
            // now add the results from each word separately
            // but only keep results that match ALL words individually 
            for (String s : splitWords.split("\\s+")) {
                Set<Suggestion> set = suggest(artObjectTitleWords, s);
                if (wordMatches == null)
                    wordMatches = set;
                else {
                    // only keep suggestion if it exists for all words supplied
                    Set<Suggestion> copyMatches = CollectionUtils.newHashSet();
                    for (Suggestion sug : wordMatches) {
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
        if (artistName != null && artistName.length() > 0) {
            for (Suggestion artistSug : suggestNameSet(artistName,artistAltNames) ) {
                if (worksIDs == null)
                    worksIDs = CollectionUtils.newHashSet();
                //log.info("artist: " + artistSug.entityID);
                Constituent c = fetchByConstituentID(artistSug.entityID);
                worksIDs.addAll(c.getWorksIDs());
            }
        }
        if (worksIDs != null) {
            // only keep suggestions that also match the suggestions 
            // for the given artist name
            Set<Suggestion> set = CollectionUtils.newHashSet();
            if (matches != null) {
                for (Suggestion oSug : matches) {
                    //log.info("title:" + oSug.string + " id:" + oSug.entityID);
                    if (worksIDs.contains(Long.valueOf(oSug.entityID)))
                        set.add(oSug);
                }
                matches = set;
            }
        }

        // now we need to remove duplicates and sort w.r.t. the given
        // string
        Set<String> primaryTitles = CollectionUtils.newHashSet();
        Set<String> secondaryTitles = CollectionUtils.newHashSet();
        String title = StringUtils.removeDiacritics(cleanupForMatching(titleWords));
        if (matches != null) {
            for (Suggestion suggestion : matches) {
                String sugg = StringUtils.removeDiacritics(cleanupForMatching(suggestion.string));
                if (sugg.startsWith(title))
                    primaryTitles.add(StringUtils.removeMarkup(suggestion.string, false));
                else
                    secondaryTitles.add(StringUtils.removeMarkup(suggestion.string, false));
            }
        }
            
        // remove any duplicates
        for (String s : primaryTitles) {
            if (secondaryTitles.contains(s))
                secondaryTitles.remove(s);
        }
        
        // sort the two lists independently and then combine them  
        List<String> suggestions = CollectionUtils.toSortedAlphaDiacriticNormalizedList(primaryTitles);
        suggestions.addAll(CollectionUtils.toSortedAlphaDiacriticNormalizedList(secondaryTitles));

        return suggestions;
    }
    
    private void consumeIndexPair(Map<String, Set<Suggestion>> map, String key, String value, Long entityID) {
        if (value != null && value.length() > 0) {
            if (key != null && key.length() > 0) {  //TODO review putting a minimum length here
                Set<Suggestion> suggestions = map.get(key);
                if (suggestions == null) {
                    suggestions = CollectionUtils.newHashSet();
                    map.put(key, suggestions);
                }
                suggestions.add(new Suggestion(value, entityID));
            }
        }
    }
    
    private void consumeIndexPair(Map<String,Set<Suggestion>> map, String value, Long entityID) {
        if (value != null && value.length() > 0) {
            String key = cleanupForMatching(value);
            // include the full value as it's own "word" so we can try to match
            // the whole thing at once if we want - this would be the first
            // part of suggest I guess //TODO review this
            consumeIndexPair(map, key, value, entityID);
            // split the cleaned up key on white space and index each word separately
            for (String word : key.split("\\s+")) {
                consumeIndexPair(map, word, value, entityID);
            }
            
            //Now strip out any non letters/white space for matching
            String strippedName = cleanupForLettersOnlyMatching(key);
            if (!strippedName.equals(key))
            {
                //log.debug("stripped: " + key + " :: " + strippedName + "(" + entityID + ")");
                consumeIndexPair(map, strippedName, value, entityID);
                // split the cleaned up key on white space and index each word separately
                for (String word : strippedName.split("\\s+")) {
                    consumeIndexPair(map, word, value, entityID);
                }
            }
        }
    }

    synchronized private Map<String, Set<Suggestion>> parseArtistAltNames(Map<Long, Constituent> newConstituents) {
    	Map<String, Set<Suggestion>> newArtistAltNames = CollectionUtils.newTreeMap(
                new Comparator<String>() {
                    public int compare(String a, String b) {
                        return StringUtils.getDefaultCollator().compare(a, b);
                    }
                }
        );

        if (newConstituents != null) {
            for (Constituent c : newConstituents.values()) {
                if (c.isArtistOfNGAObject()) {
                    consumeIndexPair(newArtistAltNames, c.getPreferredDisplayName(), c.getConstituentID());
                    if (c.getAltNames() != null) {
                        for (ConstituentAltName a: c.getAltNames()) {
                            consumeIndexPair(newArtistAltNames, a.getDisplayName(), c.getConstituentID());
                        }
                    }
                }
            }
            log.info("Done with parseArtistAltNames");
        }
    	return newArtistAltNames;
    }

    synchronized private Map<String, Set<Suggestion>> parseOwnerAltNames(Map<Long, Constituent> newConstituents) {
    	Map<String, Set<Suggestion>> newOwnerAltNames = CollectionUtils.newTreeMap(
                new Comparator<String>() {
                    public int compare(String a, String b) {
                        return StringUtils.getDefaultCollator().compare(a, b);
                    }
                }
        );
        
        if (newConstituents != null) {
            for (Constituent c : newConstituents.values()) {
                if (c.isPreviousOwnerOfNGAObject()) {
                    consumeIndexPair(newOwnerAltNames, c.getPreferredDisplayName(), c.getConstituentID());
                    if (c.getAltNames() != null) {
                        for (ConstituentAltName a: c.getAltNames()) {
                            consumeIndexPair(newOwnerAltNames, a.getDisplayName(), c.getConstituentID());
                        }
                    }
                }
                
            }
            log.info("Done with parseOwnerAltNames");
        }
    	return newOwnerAltNames;
    }

    private Set<Suggestion> suggest(Map<String, Set<Suggestion>> map, String string) {
        Set<Suggestion> eSet = CollectionUtils.newHashSet();
        if (map != null && string != null && string.length() > 0) {

            // remove any non-characters except for ' and - before we look up the string in our map
            String base = cleanupForMatching(string);
            //String base = string.toLowerCase().replaceAll("[^-\'\\p{L}]", " ");
        
            if (base.length() > 0) {
                TreeMap<String, Set<Suggestion>> t = (TreeMap<String, Set<Suggestion>>) map;

                // set the start and end keys for the sub map call
                // which collects all keys within a range
                // endKey is just the base with an extra character (the largest possible one)
                // which we will never use anyway due to our data
                // so this should be pretty safe to use
                String startKey = base;
                String endKey = base + Character.MAX_VALUE;
//              log.info("DDDDDDDDDDDDDDDDDDDDDDDDDDDDD: Searching for: " + startKey);
                Map<String, Set<Suggestion>> sMap = t.subMap(startKey, endKey);
                for (Set<Suggestion> set : sMap.values()) {
                    eSet.addAll(set);
                }
            }
        }
        return eSet;
    }

    private Set<Suggestion> suggestNameSet(String baseName, Map<String, Set<Suggestion>> dataMap) {
        // suggest based on the full string entered
        Set<Suggestion> suggestions = suggest(dataMap, baseName);

        // now process each word separately and come up with another
        // list of suggestions where all terms match each suggestion
        String splitString = cleanupForMatching(baseName);
        Set<Suggestion> wordMatches = null;
        for (String name : splitString.split("\\s+")) {
            Set<Suggestion> set = suggest(dataMap, name);
            if (wordMatches == null)
                wordMatches = set;
            else {
                // only keep suggestion if it exists for all the words supplied
                Set<Suggestion> copyMatches = CollectionUtils.newHashSet();
                for (Suggestion sug : wordMatches ) {
                    if (set.contains(sug))
                        copyMatches.add(sug);
                }
                wordMatches = copyMatches;
            }
        }
        
        // add the suggestions from word matching to the set of suggestions
        if (suggestions != null && wordMatches != null)
            suggestions.addAll(wordMatches);
        
        return suggestions;
    }
    
    private List<Suggestion> suggestSuggestions(String baseName, Set<Suggestion> suggestions) {
        // be sure data is loaded before we let the API get used
        isDataReady(true);

        List<Suggestion> combined = new ArrayList<Suggestion>();
        // prioritize based on closeness of match to given name
        Set<Suggestion> bestNames = CollectionUtils.newHashSet();
        Set<Suggestion> otherNames = CollectionUtils.newHashSet();
        List<Suggestion> otherNamesList = new ArrayList<Suggestion>();
        
        if (suggestions != null) {
            // now, prioritize the suggestions based on names that
            // match the given string (
            String base = StringUtils.removeDiacritics(cleanupForMatching(baseName));
            for (Suggestion suggestion : suggestions) {
                String sugg = StringUtils.removeDiacritics(cleanupForMatching(suggestion.string));
                if (sugg.startsWith(base)) {
                    bestNames.add(suggestion);
                }
                else {
                    otherNames.add(suggestion);
                }
            }
            // remove duplicate string values
            for (Suggestion s : bestNames) {
                if (otherNames.contains(s))
                    otherNames.remove(s);
            }
            combined.addAll(bestNames);
            Collections.sort(combined, suggestionAlphaDiacriticNormalizedComparator);
            otherNamesList.addAll(otherNames);
            Collections.sort(otherNamesList, suggestionAlphaDiacriticNormalizedComparator);
            combined.addAll(otherNamesList);
        }
        
        return combined;
    }
    
    public List<Suggestion> suggestArtObjectsByArtistName(String baseName) {
    	List<Suggestion> constituentSuggestions = suggestSuggestions(baseName, suggestNameSet(baseName, artistAltNames));
    	List<Suggestion> objectSuggestions = CollectionUtils.newArrayList();
    	for (Suggestion s: constituentSuggestions) {
    		Constituent c = fetchByConstituentID(s.getEntityID());
    		if (c != null) {
    			for (Long id : c.getWorksIDs()) {
    				objectSuggestions.add(new Suggestion(s.getString(),id));
    			}
    		}
    	}
    	return objectSuggestions;
    }

    public List<Suggestion> suggestArtObjectsByTitle(String baseName) {
    	return suggestSuggestions(baseName, suggestNameSet(baseName, artObjectTitleWords));
    }

    private List<String> suggestNames(String baseName, Set<Suggestion> suggestions) {
        
        List<Suggestion> rslts = suggestSuggestions(baseName, suggestions);
        List<String> rsltList = new ArrayList<String> ();

        if (rslts != null) {
            for (Suggestion s: rslts)
            {
                rsltList.add(s.string);
            }
        }
        return rsltList;
    }
    
    public List<String> suggestArtistNames(String baseName) {
        return suggestNames(baseName, suggestNameSet(baseName, artistAltNames));
    }

    public List<String> suggestOwnerNames(String baseName) {
        return suggestNames(baseName, suggestNameSet(baseName, ownerAltNames));
    }
    
    public Map<Long, String> suggestOwners(String baseName) {
        log.debug("Suggest Owners called with param: " + baseName);
        LinkedHashMap<Long, String> rslts = new LinkedHashMap<Long, String>();
        List<Suggestion> suggestions = suggestSuggestions(baseName, suggestNameSet(baseName, ownerAltNames));
        if (suggestions != null)
        {
            for (Suggestion s: suggestions)
            {
                rslts.put(s.entityID, s.string);
            }
        }
        log.debug("Suggest Owners hits: " + rslts.size());
        return rslts;
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
