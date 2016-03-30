package gov.nga.search;

import gov.nga.performancemonitor.PerformanceMonitor;
import gov.nga.performancemonitor.PerformanceMonitorFactory;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.SystemUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
 
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchHelper <T extends Faceted & Searchable & Sortable> 
	implements CustomHash {

	private static final Logger log = LoggerFactory.getLogger(SearchHelper.class);
	private static boolean disableCaching = true;

	public static enum SEARCHOP {
		// art object search fields
		STARTSWITH,
		LIKE,
		EQUALS,
		BETWEEN,
		IN,
		INTERSECTS
	}

	private List<SearchFilter> filters = CollectionUtils.newArrayList(); 
	private List<FreeTextFilter> freeTextFilters = CollectionUtils.newArrayList();
	private FreeTextSearchable<T> searchServicer = null;
	
	private class ResultSet {
		List<Object> list;
		Calendar created = Calendar.getInstance(); 
		int hits = 0;
	}
	
	private static Map<String, Object> cachedSearches = CollectionUtils.newHashMap();
	private static Calendar lastCacheCheck = Calendar.getInstance();
	
	public long customHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(11,13);
		for (SearchFilter f : filters) {
			hcb.append(f.customHash());
		}
		for (FreeTextFilter f : freeTextFilters) {
			hcb.append(f.customHash());
		}
		// hcb.append(searchServicer); this won't matter because
		// the only difference is with T in these classes
		return hcb.hashCode();
	}
	
	private String customCacheHash(List<T> list, SortHelper<T> sortH) {
		HashCodeBuilder hcb = new HashCodeBuilder(23,29);
		hcb.append(list);
		hcb.append(sortH.customHash());
		hcb.append(customHash());
		String cacheKey = hcb.hashCode() + ":";
		if (list != null)
			cacheKey += list.size();
		cacheKey += ":";
		if (sortH != null)
			cacheKey += sortH.customHash();
		cacheKey += ":" + customHash();
		return cacheKey;
	}
	
	public SearchHelper() {
		super();
	}
	
	public SearchHelper(Object... order) {
		this();
	}
	
	public SearchFilter addFilter(Object s, SEARCHOP o, String v1) {
		SearchFilter filter = new SearchFilter(o, s, v1);
		filters.add(filter);
		return filter;
	}
	
	public SearchFilter addFilter(Object s, SEARCHOP o, List<String> strings) {
		SearchFilter filter = new SearchFilter(o,s,strings);
		filters.add(filter);
		return filter; 
	} 

	public SearchFilter addFilter(Object s, SEARCHOP o, List<String> strings, Boolean normalize) {
		SearchFilter filter = new SearchFilter(o,s,strings,normalize);
		filters.add(filter);
		return filter; 
	}
	
	public SearchFilter addFilter(Object s, SEARCHOP o, String v1, String v2) {
		SearchFilter filter = new SearchFilter(o, s, v1, v2);
		filters.add(filter);
		return filter;
	}
    
	public SearchFilter addFilter(Object s, SEARCHOP o, String v1, Boolean normalize) {
		SearchFilter filter = new SearchFilter(o, s, v1, normalize);
		filters.add(filter);
		return filter;
	}
    
	public SearchFilter addFilter(Object s, SEARCHOP o, String v1, String v2, Boolean normalize) {
		SearchFilter filter = new SearchFilter(o, s, v1, v2, normalize);
		filters.add(filter);
		return filter;
	}
	
	public void addFilter(SearchFilter filter) {
		filters.add(filter);
	}
	
	public void setFreeTextServicer(FreeTextSearchable<T> searchServicer) {
		//log.debug("FreeTextSearcher set: " + searchServicer);
		this.searchServicer = searchServicer;
	}
	
	public <F> void addFreeTextFilter (F fieldToSearch, String searchTerm) {
		//log.info("SSSSSSSS: GETS HERE TWO");
		freeTextFilters.add(new FreeTextFilter(fieldToSearch, searchTerm));
	}

	public <F> void addFreeTextFilter(List<F> fieldsToSearch, String searchTerm) {
		//log.info("SSSSSSSS: GETS HERE ONE");
		freeTextFilters.add(new FreeTextFilter(fieldsToSearch, searchTerm));
	}
	
	public int getFilterSize()
	{
		return filters.size() + freeTextFilters.size();
	}
	
	private List<T> getSortedListFromCache(List<T> list, SortHelper<T> sortH) {
		if (disableCaching)
			return null;
		Calendar now = Calendar.getInstance();
		boolean checkCache = false;
		synchronized (this) {
			if (now.getTimeInMillis() - lastCacheCheck.getTimeInMillis() > 60000) {
				checkCache = true;
				lastCacheCheck = Calendar.getInstance();
			}
		}
		if (checkCache) {
			log.info("Checking age of search cache");
			// start eliminating from cache anything
			// older than five minutes
			List<String> delList = CollectionUtils.newArrayList();
			for (String s : cachedSearches.keySet()) {
				ResultSet r = (ResultSet) cachedSearches.get(s);
				if ( now.getTimeInMillis() - r.created.getTimeInMillis() > 600000 )
					delList.add(s);
			}
			// delete any cached results greater than five minutes old
			// really, this should be triggered by an external
			// party, but no biggie for now
			for (String s : delList) {
				cachedSearches.remove(s);
			}
		}
		
		String cacheKey = customCacheHash(list, sortH);
		ResultSet result = null;
		synchronized (cachedSearches) {
			result = (ResultSet) cachedSearches.get(cacheKey);
		}
		if (result != null) {
			result.hits++;
			try {
				log.info("Found cached result set for key " + cacheKey + ". Attempting to use it.");
				@SuppressWarnings("unchecked")
				List<T> res = (List) result.list;
				//log.info("Cast result set without error.  Returning it");
				return res;
			}
			catch (ClassCastException ce) {
				// oops - we must have a hash collision here
				// so unset any entries for cacheKey
				saveSortedListToCache(list,sortH,null);
			}
		}
		return null;
	}
	
	private void saveSortedListToCache(List<T> list, SortHelper<T> sortH, List<T> value) {
		if (disableCaching)
			return;
		String cacheKey = customCacheHash(list, sortH);
		//log.info("hash is: " + cacheKey);
		@SuppressWarnings("unchecked")
		List<Object> val = (List) value;
		ResultSet res = new ResultSet();
		res.list = val;
		synchronized (cachedSearches) {
			if (val == null)
				cachedSearches.remove(cacheKey);
			else {
				log.info("size of cache now: " + cachedSearches.size());
				log.info(SystemUtils.freeMemorySummary());
				cachedSearches.put(cacheKey, res);
			}
		}
	}

	
	private List<T> searchExec(List<T> list, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH) {

		PerformanceMonitor perfMonitor = PerformanceMonitorFactory.getMonitor(SearchHelper.class);
		List<T> matches = getSortedListFromCache(list, sortH);
		log.debug("value of match after cache fetch: " + matches);
		perfMonitor.logElapseTimeFromLastReport("Pulled matches from cache");
		if (matches == null) {
			// not cached
			matches = CollectionUtils.newArrayList();

			// first we assemble a list of all matching objects, appending as we go which presumably
			// will be faster than removing from the list we were provided
			if (list != null) {
				for (T a : list) {
					boolean addit = true;
					for (SearchFilter sf : filters) {
						Boolean match = a.matchesFilter(sf); 
						// if, in looping through the list of filters, we find that one of the filters
						// cannot be matched or can be matched and doesn't match, then we set the flag
						// to false and abort the inner loop
						if (match == null || !match) {
							addit = false;
							break;
						}
					}
					if (addit)
						matches.add(a);
				}
			}
			perfMonitor.logElapseTimeFromLastReport("matches built for new cache");
			// sort the art entities if a sort helper is provided
			if (sortH != null) {
				sortH.sortEntities(matches);
				perfMonitor.logElapseTimeFromLastReport("matches sorted for new cache");
			}
			// save result to cache
			saveSortedListToCache(list, sortH, matches);
			perfMonitor.logElapseTimeFromLastReport("new cache saved");
		}
		
		// and now we count facets if the facethelper is not null
		// we we send them back to the caller
		log.debug("Process facets: " + fn);
		perfMonitor.logElapseTimeFromLastReport("Starting work on facets");
		if (fn != null) {
            // we cast the List as a list of Faceted objects here because that's
			// all we really need it for to process the facets - I'm not sure why
			// a compiler error even results in the first place though since Faceted is
			// implemented by the class T
			@SuppressWarnings("unchecked")
			List<Faceted> fList = (List) matches;
			fn.processFacets(fList);
			perfMonitor.logElapseTimeFromLastReport("facets processed");
		}
		
		// finally, we clip the results for the pagination 
		if (pn != null)
		{
			matches = clipToPage(matches,pn);
			perfMonitor.logElapseTimeFromLastReport("matches clipped");
		}
		perfMonitor.logElapseTimeFromSeed("searchExec completed");
		return matches;
	}

	public List<T> search(List<T> baseList, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH) {
		if (freeTextFilters != null && freeTextFilters.size() > 0) {
			// start with the baseList, and prune it down further using each free text search
			// defined in the list of free text filters
			List<T> freeTextResults = CollectionUtils.newArrayList(baseList);
			for (FreeTextFilter f : freeTextFilters) {
				freeTextResults = searchServicer.freeTextSearch(f.getFields(), f.getSearchTerm(), freeTextResults);
			}
			baseList = freeTextResults;
		}
		// if we don't have any free text filters AND caller didn't supply a sort order
		// then go ahead and create one (which gives an opportunity for a default 
		// sort order to be applied to the given type - otherwise, the order is arbitrary
		// or in the case of free text searches, in descending score order
		else if (sortH == null) {
			sortH = new SortHelper<T>();
		}
		return searchExec(baseList, pn, fn, sortH);
	}

	public List<T> search(List<T> list, ResultsPaginator pn, FacetHelper fn, Object... order) {
		SortHelper<T> sortH = null;
		// if an order is actually specified, then create a new sort order,
		// otherwise call the search interface with a null SortHelper
		if (order != null && order.length > 0) {
			sortH = new SortHelper<T>();
			sortH.setSortOrder(order);
		}
		return search(list, pn, fn, sortH);
	}
	
	public List<T> clipToPage(List<T> list, ResultsPaginator pn) {
		if (list == null)
			return null;
		
		pn.setTotalResults(list.size());
		
		return CollectionUtils.newArrayList(list.subList(pn.getStartIndex(), pn.getEndIndex()));
	}

}
