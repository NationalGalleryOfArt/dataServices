package gov.nga.search;

import gov.nga.utils.CollectionUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetHelper {
	
	private static final Logger log = LoggerFactory.getLogger(FacetHelper.class);

	private List<Facet> facetList = null;
	Object[] facets = null;

	public List<Facet> getFacets() {
		return facetList;
	}
	
	public FacetHelper(Object... fs) {		
		//log.debug("Creating new facetHelper with " + fs.length + " facets");
		facets = fs;
	}
	
	public void processFacets(List<? extends Faceted> list) {

		class FacetCalculator implements Runnable {
			
			List<? extends Faceted> list = null;
			int start = 0; 
			int end = 0;
			
			public FacetCalculator(List<? extends Faceted> list, int start, int end) {
				this.list = list;
				this.start = start;
				this.end = end;
			}
			
			public void run() {
				try	{
					//PerformanceMonitor perfMonitor = PerformanceMonitorFactory.getMonitor(FacetHelper.class);
					for (int j=start; j<end; j++) {
						Faceted a = list.get(j);
						//perfMonitor.logElapseTimeFromLastReport("starting processing of " + a);
	    				for (Facet f : facetList) {
	    					//log.debug("Processing " + f.getFacet() + ": " + a.getClass().getCanonicalName());
	    					//perfMonitor.logElapseTimeFromLastReport("processing facet " + f.getFacet());
	    					f.processEntity(a);
	    					//perfMonitor.logElapseTimeFromLastReport("processed facet " + f.getFacet());
	    				}
						//perfMonitor.logElapseTimeFromLastReport("finished processing of " + a);
	    			}
					//perfMonitor.logElapseTimeFromSeed("threaded helper completed");
				}
				catch (Exception e)
				{
					log.error("Exception processing facet counts", e);
				}
			}
		}
		
		facetList = CollectionUtils.newArrayList();
		for (Object f : facets) {
			//log.debug("Facetizing object: " + f);
			facetList.add(new Facet(f)); 
		}
		Calendar at = Calendar.getInstance();

		if (list != null && list.size() > 0) {
			
			int threads = Runtime.getRuntime().availableProcessors();

			// if we have a very small list, then we only use
			// as many threads as we have items in our list
			if (threads > list.size())
				threads = list.size();
			//threads = 1;

			ExecutorService ex = Executors.newFixedThreadPool(threads);

			int chunk = list.size() / threads;
			for (int c=0; c<threads; c++) {
				int start = c*chunk;
				int end = (c+1)*chunk;
				if (c == threads-1)
					end = list.size();
				ex.submit( new FacetCalculator(list, start, end) );		
			}
			
			// break into two lists for faster calculating across multiple threads
			try {
				//ex.
				ex.shutdown();
				// we don't want to wait longer than 60 seconds for any
				// faceting calculations
				ex.awaitTermination(60, TimeUnit.SECONDS);
			}
			catch (InterruptedException ie) {
				log.warn("TTTTTTTTT: Terminated due to timeout of faceting calculation");
				// nothing needed here
			}
			//log.debug("thread is: " + Thread.currentThread().getId());
			log.debug("iterated through " + list.size() + " objects");
		}
		Calendar bt = Calendar.getInstance();
		log.debug("Facet calculation took " + ( bt.getTimeInMillis() - at.getTimeInMillis() ) + " milliseconds: ");

	}
	
}
