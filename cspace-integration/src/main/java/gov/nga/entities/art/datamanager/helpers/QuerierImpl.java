package gov.nga.entities.art.datamanager.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.ArtObject.SORT;
import gov.nga.common.entities.art.ArtObjectConstituent;
import gov.nga.common.entities.art.ArtObjectFactoryImpl;
import gov.nga.common.entities.art.ArtObjectMapComparator;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.DataNotReadyException;
import gov.nga.common.entities.art.Department;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.Media;
import gov.nga.common.entities.art.Place;
import gov.nga.common.entities.art.QueryResult;
import gov.nga.common.entities.art.QueryResultSuggestion;
import gov.nga.common.entities.art.SuggestType;
import gov.nga.common.entities.art.QueryResultArtData;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.entities.art.factory.ArtObjectFactory;
import gov.nga.common.entities.art.factory.ConstituentFactory;
import gov.nga.common.entities.art.factory.ConstituentFactoryImpl;
import gov.nga.common.entities.art.factory.ExhibitionArtObjectFactory;
import gov.nga.common.entities.art.factory.ExhibitionArtObjectFactoryImpl;
import gov.nga.common.entities.art.factory.ExhibitionFactory;
import gov.nga.common.entities.art.factory.ExhibitionFactoryImpl;
import gov.nga.common.entities.art.factory.LocationFactory;
import gov.nga.common.entities.art.factory.LocationFactoryImpl;
import gov.nga.common.imaging.NGAImage;
import gov.nga.common.performancemonitor.PerformanceMonitor;
import gov.nga.common.performancemonitor.PerformanceMonitorFactory;
import gov.nga.common.search.FacetHelper;
import gov.nga.common.search.FreeTextSearchable;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.search.SearchHelper;
import gov.nga.common.search.SortHelper;
import gov.nga.common.suggest.Suggest;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.MutableInt;
import gov.nga.common.utils.StringUtils;
import gov.nga.common.entities.art.ArtEntityFreeTextSearch;
import gov.nga.entities.art.datamanager.data.QueryResultFactory;
import gov.nga.entities.art.datamanager.data.QueryResultNGAImage;
import gov.nga.imaging.dao.NetXImageDAO;

public class QuerierImpl implements ArtDataQuerier
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(QuerierImpl.class);
	
	private static ArtObjectFactory<ArtObject> artObjFactory = new ArtObjectFactoryImpl();
    private static ConstituentFactory<Constituent> constFactory = new ConstituentFactoryImpl();
    private static LocationFactory<Location> locationFactory = new LocationFactoryImpl();
    private static ExhibitionFactory<Exhibition> exhFactory = new ExhibitionFactoryImpl();
    private static ExhibitionArtObjectFactory<ExhibitionArtObject> exhAOFactory = new ExhibitionArtObjectFactoryImpl();
    
	private ArtDataCacher dataCache;
	private NetXImageDAO imageDAO;
	
	private List<ExhibitionArtObject> exhArtObjectsCache = CollectionUtils.newArrayList();
	
	public QuerierImpl(final ArtDataCacher ac)
	{
		dataCache = ac;
		createExhibitionArtObjectsCache(dataCache);
	}
	
	public QuerierImpl()
	{
		
	}
	
	private List<ExhibitionArtObject> getExhibitionArtObjectsCache()
	{
		if (exhArtObjectsCache == null || exhArtObjectsCache.size() == 0)
			createExhibitionArtObjectsCache(dataCache);
		
		return exhArtObjectsCache;
	}
	
	private void createExhibitionArtObjectsCache(final ArtDataCacher ac)
	{
		if (ac.getIsDataReady())
		{
			List<ExhibitionArtObject> newCache = CollectionUtils.newArrayList();
			for (Exhibition exh: dataCache.getExhibitionsRaw())
			{
				newCache.addAll(exh.getExhibitionObjects());
			}
			
			synchronized(exhArtObjectsCache)
			{
				exhArtObjectsCache = newCache;
			}
			
			log.info(String.format("Exhibitions ArtObject Cache size (init): %d", exhArtObjectsCache.size()));
		}
	}
	
	public void setImageDAO(NetXImageDAO dao)
	{
		imageDAO = dao;
	}

	public void setArtDataCacher(ArtDataCacher cacher)
	{
		dataCache = cacher;
		createExhibitionArtObjectsCache(cacher);
	}

	@Override
	public QueryResultArtData<ArtObject> fetchByObjectID(long objectID) throws DataNotReadyException 
	{
		return fetchByObjectID(objectID, artObjFactory);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> fetchByObjectID(long objectID, ArtObjectFactory<T> factory)
			throws DataNotReadyException 
	{
		final ArtObject obj = dataCache.getArtObjectMap().get(objectID);
		final List<T> results = CollectionUtils.newArrayList();
		if (obj != null)
		{
			results.add(factory.createObject(obj));
		}
		return QueryResultFactory.createLocalArtObjectResult(results);
	}

	@Override
	public QueryResultArtData<ArtObject> fetchByObjectIDs(Collection<Long> objectIDs) throws DataNotReadyException 
	{
		return fetchByObjectIDs(objectIDs, artObjFactory);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> fetchByObjectIDs(Collection<Long> objectIDs,
			ArtObjectFactory<T> factory) throws DataNotReadyException 
	{
		final List<T> results = CollectionUtils.newArrayList();
		if (objectIDs != null)
		{
			final PerformanceMonitor taskMonitor = PerformanceMonitorFactory.getMonitor(this.getClass() + objectIDs.toString());
			for (Long id: objectIDs)
			{
				ArtObject obj = dataCache.getArtObjectMap().get(id);
				if (obj != null)
				{
					taskMonitor.logElapseTimeFromLastReport("Cache fetch of object: " + id);
					results.add(factory.createObject(obj));
					taskMonitor.logElapseTimeFromLastReport("Added to queue object: " + id);
				}
			}
			taskMonitor.logElapseTimeFromLastReport("Collection built");
		}
		return QueryResultFactory.createLocalArtObjectResult(results);
	}

	@Override
	public QueryResultArtData<ArtObject> fetchByObjectIDs(Collection<Long> objectIDs, SORT... order)
			throws DataNotReadyException 
	{
		return fetchByObjectIDs(objectIDs, artObjFactory, order);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> fetchByObjectIDs(Collection<Long> objectIDs,
			ArtObjectFactory<T> factory, SORT... order) throws DataNotReadyException 
	{
		final List<T> results = CollectionUtils.newArrayList();
		if (objectIDs != null)
		{
			for (Long id: objectIDs)
			{
				ArtObject obj = dataCache.getArtObjectMap().get(id);
				if (obj != null)
				{
					results.add(factory.createObject(obj));
				}
			}
			// sort the list of objects before returning it
	        SortHelper<T> sh = new SortHelper<T>();
	        sh.sortArtEntities(results, (Object[]) order);
		}
		return QueryResultFactory.createLocalArtObjectResult(results);
	}

	@Override
	public QueryResultArtData<ArtObject> fetchObjectsByRelationships(List<ArtObjectConstituent> ocs) 
	{
		return fetchObjectsByRelationships(ocs, artObjFactory);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> fetchObjectsByRelationships(List<ArtObjectConstituent> ocs,
			ArtObjectFactory<T> factory) 
	{
		final List<T> results = CollectionUtils.newArrayList();
        if (ocs != null) 
        {
    		final Map<Long, T> aos = CollectionUtils.newHashMap();
            for (ArtObjectConstituent oc : ocs) 
            {
                Constituent c = oc.getConstituent();
                if (c != null) 
                {
                	List<ArtObject> works = fetchByObjectIDs(c.getWorksIDs(Constituent.ARTISTWORKRELATIONS.ALLWORKS.getDataLabel())).getResults();
                    for (ArtObject o : works)
                    {
                        aos.put(o.getObjectID(), factory.createObject(o));
                    }
                }
            }
            results.addAll(aos.values());
        }
		return QueryResultFactory.createLocalArtObjectResult(results);
	}

	@Override
	public QueryResultArtData<ArtObject> searchArtObjects(SearchHelper<ArtObject> sh,
			ResultsPaginator pn, FacetHelper fn, Enum<?>... order) throws DataNotReadyException 
	{
		return searchArtObjects(sh, pn, fn, artObjFactory, order);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> searchArtObjects(SearchHelper<T> sh,
			ResultsPaginator pn, FacetHelper fn, ArtObjectFactory<T> factory, Enum<?>... order)
					throws DataNotReadyException 
	{
		// TODO Look into FreeText Searcher
		return searchArtObjects(sh, pn, fn, factory, new ArtEntityFreeTextSearch<T>(), order);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> searchArtObjects(SearchHelper<T> searchH,
			ResultsPaginator pn, FacetHelper fn, ArtObjectFactory<T> factory, FreeTextSearchable<T> freeTextSearcher,
			Enum<?>... order) throws DataNotReadyException 
	{
		SortHelper<T> sh = null;
        if (order != null && order.length > 0) {
            sh = new SortHelper<T>();
            sh.setSortOrder(order);
        }
        return searchArtObjects(searchH, pn, fn, sh, factory, freeTextSearcher);
	}

	@Override
	public QueryResultArtData<ArtObject> searchArtObjects(SearchHelper<ArtObject> sh,
			ResultsPaginator pn, FacetHelper fn, SortHelper<ArtObject> sortH) throws DataNotReadyException 
	{
		return searchArtObjects(sh, pn, fn, sortH, artObjFactory);
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> searchArtObjects(SearchHelper<T> sh,
			ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH, ArtObjectFactory<T> factory)
					throws DataNotReadyException 
	{
		// TODO FreeText Searcher
		return searchArtObjects(sh, pn, fn, sortH, factory, new ArtEntityFreeTextSearch<T>());
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> searchArtObjects(SearchHelper<T> searchH,
			ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH, ArtObjectFactory<T> factory,
			FreeTextSearchable<T> freeTextSearcher) throws DataNotReadyException 
	{
		if(!dataCache.getIsDataReady())
		{
            throw new DataNotReadyException("TMS data not loaded into memory.");
		}

        searchH.setFreeTextServicer(freeTextSearcher);
        List<T> list = CollectionUtils.newArrayList();
        if (factory == null)
        {
        	factory = (ArtObjectFactory<T>) artObjFactory;
        }
        for (ArtObject obj: dataCache.getArtObjectsRaw())
        {
            list.add(factory.createObject(obj));
        }
		return QueryResultFactory.createLocalArtObjectResult(searchH.search(list, pn, fn, sortH), pn, 
											(fn == null) ? Collections.emptyList() : fn.getFacets());
	}

	@Override
	public <T extends ArtObject> QueryResultArtData<T> fetchRelatedWorks(ArtObject baseO, ArtObjectFactory<T> factory)
			throws DataNotReadyException 
	{
		final List<T> results = CollectionUtils.newArrayList();
		if (baseO != null) 
		{
        
            Map<String, MutableInt> nationalities = baseO.getNationalities();
            Map<T, Long> m = CollectionUtils.newHashMap();
            ArtObjectMapComparator<T> smc = new ArtObjectMapComparator<T>(m);
            Map<T,Long> sm = CollectionUtils.newTreeMap(smc);
    
    
            if (baseO != null) {
                // common styles
                for (ArtObject o : dataCache.getArtObjectsRaw()) 
                {
                    if (!baseO.getObjectID().equals(o.getObjectID()))
                    {
                        Long score = baseO.relatedTotalScore(o, nationalities);
                        if (score > 0)
                        {
                            m.put(factory.createObject(o), score);
                        }
                    }
                }
    
                // now, populate the sortable map
                sm.putAll(m);
                results.addAll(sm.keySet());
            }
        }
		return QueryResultFactory.createLocalArtObjectResult(results);
	}

	@Override
	public QueryResultArtData<Constituent> fetchByConstituentID(long cID) 
	{
		return fetchByConstituentID(cID, constFactory);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> fetchByConstituentID(long cID, ConstituentFactory<C> factory) 
	{
		final List<C> results = CollectionUtils.newArrayList();
		final Constituent c = dataCache.getConstituentMap().get(cID);
		if (c != null)
		{
			results.add(factory.createObject(c));
		}
		return QueryResultFactory.createLocalConstituentResult(results);
	}

	@Override
	public QueryResultArtData<Constituent> fetchByConstituentIDs(Collection<Long> objectIDs,
			gov.nga.common.entities.art.Constituent.SORT... order) 
	{
		return fetchByConstituentIDs(objectIDs, constFactory, order);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> fetchByConstituentIDs(Collection<Long> objectIDs,
			ConstituentFactory<C> factory, gov.nga.common.entities.art.Constituent.SORT... order) 
	{
		final List<C> results = CollectionUtils.newArrayList();
		if (objectIDs != null)
		{
			for (Long id: objectIDs)
			{
				Constituent c = dataCache.getConstituentMap().get(id);
				if (c != null)
				{
					results.add(factory.createObject(c));
				}
			}
			// sort the list of objects before returning it
	        SortHelper<C> sh = new SortHelper<C>();
	        sh.sortArtEntities(results, (Object[]) order);
		}
		return QueryResultFactory.createLocalConstituentResult(results);
	}

	@Override
	public QueryResultArtData<Constituent> fetchByConstituentIDs(Collection<Long> objectIDs) 
	{
		return fetchByConstituentIDs(objectIDs, constFactory);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> fetchByConstituentIDs(Collection<Long> objectIDs,
			ConstituentFactory<C> factory) 
	{
		final List<C> results = CollectionUtils.newArrayList();
		if (objectIDs != null)
		{
			for (Long id: objectIDs)
			{
				Constituent c = dataCache.getConstituentMap().get(id);
				if (c != null)
				{
					results.add(factory.createObject(c));
				}
			}
		}
		return QueryResultFactory.createLocalConstituentResult(results);
	}

	@Override
	public QueryResultArtData<Constituent> searchConstituents(SearchHelper<Constituent> sh,
			ResultsPaginator pn, FacetHelper fn, Enum<?>... order) 
	{
		return searchConstituents(sh, pn, fn, constFactory, order);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> searchConstituents(SearchHelper<C> searchH,
			ResultsPaginator pn, FacetHelper fn, ConstituentFactory<C> factory, Enum<?>... order) 
	{
		return searchConstituents(searchH, pn, fn, factory,  new ArtEntityFreeTextSearch<C>(), order);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> searchConstituents(SearchHelper<C> searchH,
			ResultsPaginator pn, FacetHelper fn, ConstituentFactory<C> factory, FreeTextSearchable<C> freeTextSearcher,
			Enum<?>... order) 
	{
		SortHelper<C> sortH = null;
        if (order != null && order.length > 0) 
        {
            sortH = new SortHelper<C>();
            sortH.setSortOrder(order);
        }
        return searchConstituents(searchH, pn, fn, sortH, factory, freeTextSearcher);
	}

	@Override
	public QueryResultArtData<Constituent> searchConstituents(SearchHelper<Constituent> sh,
			ResultsPaginator pn, FacetHelper fn, SortHelper<Constituent> sortH) 
	{
		return searchConstituents(sh, pn, fn, sortH, constFactory);
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> searchConstituents(SearchHelper<C> sh,
			ResultsPaginator pn, FacetHelper fn, SortHelper<C> sortH, ConstituentFactory<C> factory) 
	{
		return searchConstituents(sh, pn, fn, sortH, factory, new ArtEntityFreeTextSearch<C>());
	}

	@Override
	public <C extends Constituent> QueryResultArtData<C> searchConstituents(SearchHelper<C> sh,
			ResultsPaginator pn, FacetHelper fn, SortHelper<C> sortH, ConstituentFactory<C> factory,
			FreeTextSearchable<C> freeTextSearcher) 
	{
		final List<C> results = CollectionUtils.newArrayList();
        for (Constituent constObj: dataCache.getConstituentsRaw())
        {
        	results.add(factory.createObject(constObj));
        }
        sh.setFreeTextServicer(freeTextSearcher);
        return QueryResultFactory.createLocalConstituentResult(sh.search(results, pn, fn, sortH), pn, 
				(fn == null) ? Collections.emptyList() : fn.getFacets());
	}


	@Override
	public QueryResultArtData<Location> fetchByLocationID(long locationID) throws DataNotReadyException 
	{
		return fetchByLocationID(locationID, locationFactory);
	}


	@Override
	public <T extends Location> QueryResultArtData<T> fetchByLocationID(long locationID, LocationFactory<T> factory)
			throws DataNotReadyException 
	{
		return fetchByLocationIDs(Arrays.asList(new Long[]{locationID}), factory);
	}


	@Override
	public QueryResultArtData<Location> fetchByLocationIDs(List<Long> locationIDs) throws DataNotReadyException 
	{
		return fetchByLocationIDs(locationIDs, locationFactory);
	}


	@Override
	public <T extends Location> QueryResultArtData<T> fetchByLocationIDs(List<Long> locationIDs,
			LocationFactory<T> factory) throws DataNotReadyException 
	{
		List<T> results = CollectionUtils.newArrayList();
		if (locationIDs != null && factory != null)
		{
			for (Long id: locationIDs)
			{
				Location obj = dataCache.getLocationsMap().get(id);
				if (obj != null)
				{
					results.add(factory.createObject(obj));
				}
			}
		}
		return QueryResultFactory.createLocalLocationResult(results);
	}


	@Override
	public QueryResultArtData<Exhibition> fetchByExhibitionID(long id) throws DataNotReadyException 
	{
		return fetchByExhibitionID(id, exhFactory);
	}


	@Override
	public <T extends Exhibition> QueryResultArtData<T> fetchByExhibitionID(long id, ExhibitionFactory<T> factory)
			throws DataNotReadyException 
	{
		return fetchByExhibitionIDs(Arrays.asList(new Long[] {id}), factory);
	}


	@Override
	public QueryResultArtData<Exhibition> fetchByExhibitionIDs(List<Long> ids) throws DataNotReadyException 
	{
		return fetchByExhibitionIDs(ids, exhFactory);
	}


	@Override
	public <T extends Exhibition> QueryResultArtData<T> fetchByExhibitionIDs(List<Long> ids, ExhibitionFactory<T> factory)
			throws DataNotReadyException 
	{
		List<T> results = CollectionUtils.newArrayList();
		if (ids != null && factory != null)
		{
			for (Long id: ids)
			{
				Exhibition obj = dataCache.getExhibitionMap().get(id);
				if (obj != null)
				{
					results.add(factory.createObject(obj));
				}
			}
		}
		return QueryResultFactory.createLocalExhibitionResult(results);
	}

    @Override
    public QueryResultArtData<Exhibition> searchExhibitions(SearchHelper<Exhibition> srchh, ResultsPaginator pn,
            SortHelper<Exhibition> srth) throws DataNotReadyException 
    {
        return searchExhibitions(srchh, pn, srth, exhFactory);
    }

    @Override
    public <T extends Exhibition> QueryResultArtData<T> searchExhibitions(SearchHelper<T> srchh, ResultsPaginator pn,
            SortHelper<T> srth, ExhibitionFactory<T> factory) throws DataNotReadyException 
    {
    	final List<T> results = CollectionUtils.newArrayList();
        for (Exhibition exhObj: dataCache.getExhibitionsRaw())
        {
        	results.add(factory.createObject(exhObj));
        }
        srchh.setFreeTextServicer(null);
        return QueryResultFactory.createLocalExhibitionResult(srchh.search(results, pn, null, srth), pn);
    }

	@Override
	public QueryResultArtData<Place> fetchByPlaceKey(String arg0) 
	{   
        // currently, the TMS extract does not null out the location ID
        // of objects if that location is not public.  Until we do that
        // we cannot contingently expect to fetch the location when
        // it turns out to be null here because we may never get one
        // if the location isn't a public location
		List<Place> rslt = CollectionUtils.newArrayList();
		if (StringUtils.isNotBlank(arg0))
		{
			Place place = dataCache.getPlaceMap().get(arg0);
			if (place != null)
			{
				rslt.add(place);
			}
		}
		return QueryResultFactory.createLocalPlaceResult(rslt);
	}

	@Override
	public QueryResultArtData<Place> fetchByTMSLocationID(long tmsLocationID) 
	{
		List<Place> rslt = CollectionUtils.newArrayList();
		Place place = dataCache.getNewPlacesTMSLocations().get(tmsLocationID);
		if (place != null)
		{
			rslt.add(place);
		}
		return QueryResultFactory.createLocalPlaceResult(rslt);
        // currently, the TMS extract does not null out the location ID
        // of objects if that location is not public.  Until we do that
        // we cannot contingently expect to fetch the location when
        // it turns out to be null here because we may never get one
        // if the location isn't a public location
	}

	@Override
	public QueryResultArtData<Media> getMediaByEntityRelationship(String arg0) {
		List<Media> rslt = CollectionUtils.newArrayList();
		if (StringUtils.isNotBlank(arg0) && dataCache.getNewMediaRelationships().containsKey(arg0))
		{
			rslt.addAll(dataCache.getNewMediaRelationships().get(arg0));
		}
		return QueryResultFactory.createLocalMediaResult(rslt);
	}


	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggest(SuggestType type, String term) 
	{
		return QueryResultFactory.createLocalSuggestionResult(Suggest.suggestSuggestions(term, Suggest.suggestNameSet(term, dataCache.getSuggestionsRaw(type))));
	}


	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestArtObjectFromArtist(String artistName,
			String titleTerm) 
	{
        // we need to do two things here
        // 1. match on the full string that was entered for list #1
        // 2. match on each string individually and find the intersection
        // of all lists, remove any items from #2 that intersect with #1
        // 3. sort list 1 and list 2 separately then concatenate them together
        
        // match against the full string that was supplied
        Set<ArtDataSuggestion> matches = Suggest.suggest(dataCache.getSuggestionsRaw(SuggestType.ARTOBJECT_TITLE), titleTerm);
        
        String splitWords = titleTerm;
        Set<ArtDataSuggestion> wordMatches = null;
        if (splitWords != null && splitWords.length() > 0) {
            // now add the results from each word separately
            // but only keep results that match ALL words individually 
            for (String s : splitWords.split("\\s+")) {
                Set<ArtDataSuggestion> set = Suggest.suggest(dataCache.getSuggestionsRaw(SuggestType.ARTOBJECT_TITLE), s);
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
            for (ArtDataSuggestion artistSug : Suggest.suggestNameSet(artistName, dataCache.getSuggestionsRaw(SuggestType.ARTIST_TITLE)) ) 
            {
                if (worksIDs == null)
                {
                    worksIDs = CollectionUtils.newHashSet();
                }
                //log.info("artist: " + artistSug.entityID);
                QueryResultArtData<Constituent> rslt = fetchByConstituentID(artistSug.getEntityID());
                if (rslt.getResultCount() == 1)
                {
	                Constituent c = rslt.getResults().iterator().next();
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
        
        return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestArtObjectTitles(String arg0, String arg1) {
		return suggestArtObjectFromArtist(arg0, arg1);
	}

	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestArtObjects(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.ARTOBJECT_TITLE)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	@Deprecated
	public QueryResultSuggestion<ArtDataSuggestion> suggestArtistNames(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.ARTIST_TITLE_ID)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestArtists(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.ARTIST_TITLE_ID)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	@Deprecated
	public QueryResultSuggestion<ArtDataSuggestion> suggestOwnerNames(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.PROVENANCE_TITLE_ID)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestOwners(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.PROVENANCE_TITLE_ID)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	public QueryResultSuggestion<ArtDataSuggestion> suggestExhibitions(String baseName) {
		final List<ArtDataSuggestion> suggestions = Suggest.suggestSuggestions(baseName, Suggest.suggestNameSet(baseName, dataCache.getSuggestionsRaw(SuggestType.EXHIBITION_TITLE)));
		return QueryResultFactory.createLocalSuggestionResult(suggestions);
	}

	@Override
	public QueryResult<NGAImage> fetchImagesForObject(final long id) {
		return QueryResultFactory.createImageResult(imageDAO.getImagesForObject(id));
	}

	@Override
	public QueryResultArtData<Department> fetchDepartmentByCode(String arg0) throws DataNotReadyException 
	{
		return fetchDepartmentByCodes(Arrays.asList(new String[] {arg0}));
	}

	@Override
	public QueryResultArtData<Department> fetchDepartmentByCodes(List<String> arg0) throws DataNotReadyException 
	{
		final List<Department> rslts = CollectionUtils.newArrayList();
		for (Department cand: dataCache.getDepartmentsRaw())
		{
			if (cand.getDepartmentCode() != null && arg0.contains(cand.getDepartmentCode()))
			{
				rslts.add(cand);
			}
		}
		return QueryResultFactory.createLocalDepartmentResult(rslts);
	}

	@Override
	public QueryResultArtData<Department> fetchDepartmentByID(long arg0) throws DataNotReadyException 
	{
		return fetchDepartmentByIDs(Arrays.asList(new Long[] {arg0}));
	}

	@Override
	public QueryResultArtData<Department> fetchDepartmentByIDs(List<Long> arg0) throws DataNotReadyException 
	{
		final List<Department> rslts = CollectionUtils.newArrayList();
		for (Long cand: arg0)
		{
			if (dataCache.getDepartmentMap().containsKey(cand))
			{
				rslts.add(dataCache.getDepartmentMap().get(cand));
			}
		}
		return QueryResultFactory.createLocalDepartmentResult(rslts);
	}

	@Override
	public QueryResultArtData<Location> searchLocations(SearchHelper<Location> arg0, ResultsPaginator arg1,
			SortHelper<Location> arg2) throws DataNotReadyException 
	{
		return searchLocations(arg0, arg1, arg2, locationFactory);
	}

	@Override
	public <T extends Location> QueryResultArtData<T> searchLocations(SearchHelper<T> srchh, ResultsPaginator pn,
			SortHelper<T> srth, LocationFactory<T> factory) throws DataNotReadyException 
	{
		final List<T> results = CollectionUtils.newArrayList();
		LocationFactory<?> lFact = factory == null ? locationFactory : factory;
        for (Location exhObj: dataCache.getLocationsRaw())
        {
        	results.add(lFact.createObject(exhObj));
        }
        srchh.setFreeTextServicer(null);
        log.info(String.format("searchLocations(): Paginator results before call: %d", pn.getTotalResults()));
        return QueryResultFactory.createLocalLocationResult(srchh.search(results, pn, null, srth), pn);
	}

	@Override
	public QueryResultArtData<ExhibitionArtObject> searchExhibitionArtObjects(SearchHelper<ExhibitionArtObject> arg0,
			ResultsPaginator arg1, SortHelper<ExhibitionArtObject> arg2) throws DataNotReadyException 
	{
		return searchExhibitionArtObjects(arg0, arg1, arg2, exhAOFactory);
	}

	@Override
	public <T extends ExhibitionArtObject> QueryResultArtData<T> searchExhibitionArtObjects(final SearchHelper<T> srchh,
			ResultsPaginator pn, SortHelper<T> srth, ExhibitionArtObjectFactory<T> factory)
			throws DataNotReadyException 
	{
		final List<T> results = CollectionUtils.newArrayList();
        for (ExhibitionArtObject exhObj: getExhibitionArtObjectsCache())
        {
        	results.add(factory.createObject(exhObj));
        }
        srchh.setFreeTextServicer(null);
        return QueryResultFactory.createLocalExhibitionArtObjectResult(srchh.search(results, pn, null, srth), pn);
	}
}
