package gov.nga.search;

import gov.nga.performancemonitor.PerformanceMonitor;
import gov.nga.performancemonitor.PerformanceMonitorFactory;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchHelper <T extends Faceted & Searchable & Sortable> 
implements CustomHash {

	private static final Logger log = LoggerFactory.getLogger(SearchHelper.class);

	private ExecutorService searchDistributor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

	private class SearchWorker implements Callable<T> {
		T matchObj;
		List<SearchFilter> filters;

		public SearchWorker(T matchObj, List<SearchFilter> filters) {
			this.matchObj = matchObj;
			this.filters = filters;
		}

		public T call() {
			if (matchObj != null) {
				for (SearchFilter sf : this.filters) {
					Boolean match = matchObj.matchesFilter(sf); 
					// if, in looping through the list of filters, we find that one of the filters
					// cannot be matched or can be matched and doesn't match, then we set the flag
					// to false and abort the inner loop
					if (match == null || !match)
						return null;
				}
				return matchObj;
			}
			return null;
		}
	}

	private List<T> searchExec(List<T> list, ResultsPaginator pn, FacetHelper fn, SortHelper<T> sortH) {

		PerformanceMonitor perfMonitor = PerformanceMonitorFactory.getMonitor(SearchHelper.class);
		// not cached
		List<T>	matches = CollectionUtils.newArrayList();

		/* TODO - this could become significantly more efficient if we did two things
		a) sort the filters, putting the least expensive operations at the top - will potentially do that later - would need
		   expense factor on the enum I think
		b) restructure this loop to use Future<boolean> with a fixed size thread pool - would operate up to (#Cores-1) faster
		   first we assemble a list of all matching objects, appending as we go which presumably
		   will be faster than removing from the list we were provided 
		 */
		if (list != null) {
			// prepare the search work for a thread pool for maximum performance
			List<Future<T>> futures = CollectionUtils.newArrayList();
			for (T a : list) {
				Callable<T> searchWorker = new SearchWorker(a, filters);
				futures.add(searchDistributor.submit(searchWorker));
			}

			try {
				// query the results of the thread pool
				for (Future<T> f : futures) {
					T a = f.get();
					if (a != null)
						matches.add(a);
				}
			}
			catch (ExecutionException ee) {
				log.error("Error executing search query " + ee.getMessage());
			}
			catch (InterruptedException ie) {
				log.info("Search thread was interrupted" + ie.getMessage());
			}
		}
		// sort the art entities if a sort helper is provided
		if (sortH != null) {
			sortH.sortEntities(matches);
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
			List<Faceted> fList = (List<Faceted>) matches;
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
