package gov.nga.search;

import gov.nga.performancemonitor.PerformanceMonitor;
import gov.nga.performancemonitor.PerformanceMonitorFactory;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchHelper <T extends Faceted & Searchable & Sortable> implements CustomHash {

	private static final Logger log = LoggerFactory.getLogger(SearchHelper.class);

	public static enum SEARCHOP {
		// art object search fields, listed in order of ascending computational expense
		// such that we can keep our search operations sorted in order of least expensive first
		EQUALS,
		IN,
		STARTSWITH,
		BETWEEN,
		LIKE,
		INTERSECTS;
	}

	// a sorted set is used in order to automatically sort the filters by the least
	// expensive operation first since we can iterate through the filters faster
	// that way
	private Set<SearchFilter> filters = CollectionUtils.newTreeSet(getComparator()); 
	private List<FreeTextFilter> freeTextFilters = CollectionUtils.newArrayList();
	private FreeTextSearchable<T> searchServicer = null;
	
	// sort the search filters based on the operation being requested
	// so that we always start with the least expensive
	private Comparator<SearchFilter> getComparator() {
		return new Comparator<SearchFilter>() {
			// TODO an alternative approach to avoid having to suppress warnings would be to declare 
			// the enums used for SEARCHING to implement an interface. Then, the 
			// interface could be used as the declared type for the purpose of comparison 
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(SearchFilter a, SearchFilter b) {
				int res = a.getOp().compareTo(b.getOp());
				if (res == 0) {
					Enum fieldA = (Enum) a.getField();
					Enum fieldB = (Enum) a.getField();
					res = fieldA.compareTo(fieldB);
					// never return equivalence here unless the hashes are complete identical in which
					// case all of the search parameters will also be identical and there's no point in
					// having two identical search filters so the TreeSet will only keep one
					if (res == 0)
						return Long.compare(a.customHash(),b.customHash());
				}
				return res;
			}
		};
	}

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

	public Set<SearchFilter> getFilters(Object s, SEARCHOP o) {
		Set<SearchFilter> filts = CollectionUtils.newTreeSet(getComparator());
		for (SearchFilter f : getFilters())
			if (f.getField().equals(s) && f.getOp().equals(o))
				filts.add(f);
		return filts;
	}

	public Set<SearchFilter> getFilters(Object s) {
		Set<SearchFilter> filts = CollectionUtils.newTreeSet(getComparator());
		for (SearchFilter f : getFilters())
			if (f.getField().equals(s))
				filts.add(f);
		return filts;
	}
	
	public Set<SearchFilter> getFilters() {
		return this.filters;
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

	private class SearchWorker implements Callable<List<T>> {
		List<T> objList;
		Set<SearchFilter> filters;
		int start;
		int end;

		public SearchWorker(List<T> objList, int start, int end, Set<SearchFilter> filters) {
			this.objList = objList;
			this.filters = filters;
			this.start = start;
			this.end = end;
		}

		public List<T> call() {
			List<T> matches = CollectionUtils.newArrayList();
			for (int i=start; i<=end; i++) {
				T matchObj = objList.get(i);
				if (matchObj != null) {
					boolean addit=true;
					for (SearchFilter sf : this.filters) {
						Boolean match = matchObj.matchesFilter(sf); 
						// if, in looping through the list of filters, we find that one of the filters
						// cannot be matched or can be matched and doesn't match, then we set the flag
						// to false and abort the inner loop
						if (match == null || !match) {
							addit=false;
							break;
						}
					}
					if (addit)
						matches.add(matchObj);
				}
			}
			return matches;
		}
	}
	
	private List<T> searchExec(List<T> list, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH) {
		
		PerformanceMonitor perfMonitor = PerformanceMonitorFactory.getMonitor(SearchHelper.class);
		
		// create an auto sorted set to enable insertion sorting rather than
		// sorting everything at the end
		Set<T> matches = sortH.createAutoSortedSet();
		
		for (SearchFilter f : this.filters) {
			log.trace("FILTER: " + f.getOp() + " " + f.getField());
		}

		perfMonitor.resetSeedTime();
		if (list != null && list.size() > 0) {
			// prepare the search work for a thread pool for maximum performance
			
			ExecutorService searchDistributor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
			
			perfMonitor.logElapseTimeFromLastReport("starting futures");
			List<Future<List<T>>> futures = CollectionUtils.newArrayList();
			
			// divide the list and divvy it up to workers - this is more efficient than creating a separate
			// future for each item in the list
			int chunk = (list.size() / Runtime.getRuntime().availableProcessors()) - 1;
			if (chunk <= 0)
				chunk = 1;
			for (int start=0; start < list.size(); ) {
				int end = start + chunk;
				if (end >= list.size())
					end = list.size()-1;
				Callable<List<T>> searchWorker = new SearchWorker(list, start, end, filters);
				futures.add(searchDistributor.submit(searchWorker));
				start = end + 1;
			}
			perfMonitor.logElapseTimeFromLastReport("done creating futures - now collecting them");
			
			try {
				// query the results of the thread pool
				for (Future<List<T>> f : futures) {
					List<T> a = f.get();
					if (a != null && a.size() > 0)
						matches.addAll(a);
				}
			}
			catch (ExecutionException ee) {
				log.error("Error executing search query " + ee.getMessage());
			}
			catch (InterruptedException ie) {
				log.info("Search thread was interrupted" + ie.getMessage());
			}
			finally {
				searchDistributor.shutdown();
			}
			perfMonitor.logElapseTimeFromLastReport("done collecting futures");
		}
		// sort the art entities if a sort helper is provided
		/*if (sortH != null) {
			sortH.sortEntities(matches);
		}*/
		//perfMonitor.logElapseTimeFromLastReport("done sorting");

		// copy the Set into a List (hopefully the order will be retained)
		list = CollectionUtils.newArrayList(matches);
		log.trace("Total results found: " + matches.size());
		// and now we count facets if the facethelper is not null
		// we we send them back to the caller
		log.trace("Process facets: " + fn);
		if (fn != null) {
			perfMonitor.logElapseTimeFromLastReport("Starting work on facets");
			// we cast the List as a list of Faceted objects here because that's
			// all we really need it for to process the facets - I'm not sure why
			// a compiler error even results in the first place though since Faceted is
			// implemented by the class T
			
			//List<T> fList = (List<Faceted>) list;
			fn.processFacets(list);
			perfMonitor.logElapseTimeFromLastReport("facets processed");
		}
		
		// finally, we clip the results for the pagination 
		if (pn != null) {
			list = clipToPage(list,pn);
			perfMonitor.logElapseTimeFromLastReport("matches clipped");
		}
		perfMonitor.logElapseTimeFromSeed("searchExec completed");
		return list;
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

	public List<T> search(List<T> list, ResultsPaginator pn, FacetHelper fn, Object... order) throws InterruptedException, ExecutionException {
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
