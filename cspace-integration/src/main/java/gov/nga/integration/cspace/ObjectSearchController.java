package gov.nga.integration.cspace;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//import gov.nga.entities.art.ArtDataManager.Suggestion;
import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObject.SORT;
//import gov.nga.entities.art.ArtObject.SEARCH;
import gov.nga.search.ResultsPaginator;
import gov.nga.search.SearchFilter;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.search.SortHelper;

import gov.nga.utils.CollectionUtils;

import gov.nga.utils.StringUtils;


@RestController
public class ObjectSearchController {

	private static final Logger log = LoggerFactory.getLogger(ObjectSearchController.class);
	
	private static Pattern sourceMatcher = Pattern.compile("/art/(.*)/objects");
	
	private static final SORT defaultSortOrder = ArtObject.SORT.ACCESSIONNUM_ASC;

	
	// this executor helps to more quickly disperse the work that is required in order to 
	// compute base64 thumbnail values
	private ExecutorService thumbnailWorkDistributor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping(value={"/art/objects.json","/art/{source}/objects.json"})
    public ResponseEntity<Items> objectRecordsSource (
    		
    		@RequestParam(value="id", 					required=false) List<String> ids,
			@RequestParam(value="cultObj:id",			required=false) List<String> cultObj_ids,
			
			@RequestParam(value="lastModified", 		required=false) List<String> lastModified,
			@RequestParam(value="cultObj:lastModified", required=false) List<String> cultObj_lastModified,

			@RequestParam(value="artistNames", 			required=false) List<String> artistNames,
			@RequestParam(value="cultObj:artistNames", 	required=false) List<String> cultObj_artistNames,
			
			@RequestParam(value="title", 				required=false) List<String> titles,
			@RequestParam(value="cultObj:title",		required=false) List<String> cultObj_titles,

			@RequestParam(value="number", 				required=false) List<String> numbers,
			@RequestParam(value="cultObj:number",		required=false) List<String> cultObj_numbers,

			@RequestParam(value="references", 			required=false, defaultValue="true") boolean references,
			@RequestParam(value="thumbnails", 			required=false, defaultValue="true") boolean thumbnails,

			@RequestParam(value="order", 				required=false					   ) List<String> order,

			@RequestParam(value="skip",					required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",				required=false, defaultValue="50") int limit,
			
  /* 		NORMALLY, WE WOULD USE A PATH VARIABLE, BUT IT'S OPTIONAL AND SPRING DOESN'T SUPPORT THAT YET
   * 		SO WE HAVE TO PARSE THE REQUEST URI AND PULL IT OUT  		
			@PathVariable(value="source") String source,
  */
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {  	
    	
    	// validate source if present
    	processSource(request);
    	
    	// limit results to a reasonable number to encourage well behaved API usage
    	if (limit > 1000)
    		limit = 1000;
    	ResultsPaginator paginator = new ResultsPaginator(skip, limit);

    	// various helpers are used to accumulate search criteria, order, and paginate the results
    	SearchHelper<ArtObject> searchHelper = new SearchHelper<ArtObject>();
    	

    	// process all of the request parameters
    	processIDs(searchHelper, ids, cultObj_ids);
    	// these can be used if we decide to go with a "startswith" type of search rather than "contains"
    	//processSuggestableField(searchHelper, artistNames, cultObj_artistNames, ArtObject.SEARCH.ARTIST_ALLNAMES);
    	//processSuggestableField(searchHelper, titles, cultObj_titles, ArtObject.SEARCH.TITLE);
    	processTextField(searchHelper, numbers, cultObj_numbers, ArtObject.SEARCH.ACCESSIONNUM);
    	processLastModified(searchHelper, lastModified, cultObj_lastModified);
    	processTextField(searchHelper, titles, cultObj_titles, ArtObject.SEARCH.TITLE);
    	processTextField(searchHelper, artistNames, cultObj_artistNames, ArtObject.SEARCH.ARTIST_ALLNAMES);
    	
    	SortHelper<ArtObject> sortHelper = getSortHelper(order);
    	
    	// the list of items that will be returned (constructed from art object records)
		List<Item> partialResults = CollectionUtils.newArrayList();

    	// execute the search using the prepared search helper
    	List<ArtObject> artObjects = artDataManager.searchArtObjects(searchHelper, paginator, null, sortHelper);
    	
    	log.info("results size:" + artObjects.size());
    	if (artObjects.size() > 0) {
			// Map used to accumulate the thumbnail computations from Futures
    		Map<Long,Future<String>> thumbnailMap = CollectionUtils.newHashMap();
    		if (thumbnails) {
    			// submit the work to fetch thumbnails and compute base64 values of them
    			for (ArtObject o : artObjects) {
    				Callable<String> thumbWorker = new ArtObjectThumbnailWorker(o);
    				Future<String> future = thumbnailWorkDistributor.submit(thumbWorker);
    				thumbnailMap.put(o.getObjectID(), future);
    			}
    		}
    		for (ArtObject o : artObjects) {
    			URL objectURL = null;
    			try {
    				String scheme = request.getHeader("X-Forwarded-Proto");
    				if (StringUtils.isNullOrEmpty(scheme)) {
    					String sslOn = request.getHeader("X-Forwarded-SSL");
    					scheme = StringUtils.isNullOrEmpty(sslOn) ? request.getScheme() : "https"; 
    				}
    				objectURL = new URL(scheme,request.getServerName(),request.getServerPort(),"/art/tms/objects/"+o.getObjectID()+".json");
    			}
    			catch (MalformedURLException me) {
    				log.error("Problem creating object URL: " + me.getMessage());
    			}
    			Record objectRecord = new AbridgedObjectRecord(o, references);
    			Future<String> thumb = thumbnailMap.get(o.getObjectID());
    			String thumbVal = (thumb == null ? null : thumb.get());
    			partialResults.add(new Item(objectURL, thumbVal, objectRecord));
    		}
    	}
    	
    	//log.info("thumbnail cache size: " + ArtObjectThumbnailWorker.getCache().size());
		// validate the request and response accordingly if there are problems
		// assemble a well formed response
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<Items>(new Items(paginator, partialResults), headers, HttpStatus.OK);
	}
    
    // Spring REST will automatically parse values that are separated with a comma into an array
    // and will pass the individual values
    private SortHelper<ArtObject> getSortHelper(List<String> order) throws APIUsageException {
    	order = CollectionUtils.clearEmptyOrNull(order);
		List<ArtObject.SORT> orders = CollectionUtils.newArrayList();

    	if (order != null && order.size() > 0) {
    		for (String fieldName : order) {
    			if (fieldName == null)
    				throw new APIUsageException("Unspecified sort order field");

    			// detect the order (asc or desc)
    			boolean ascending = true;
    			if (fieldName.substring(0,1).equals("-")) {
    				ascending = false;
    				// strip the minus sign
    				fieldName = fieldName.substring(1);
    			}
    			String ns = NamespaceUtils.getNamespace(fieldName,ObjectRecord.getDefaultNamespace());
    			if (!ns.equals("cultObj"))
    				throw new APIUsageException("Unsupported namespace or empty field encountered in sort order");

    			fieldName = NamespaceUtils.stripNamespace(fieldName);
    			if (fieldName != null) {
    				// now we have a fieldName we should be able to sort on and we know the direction 
    				switch (fieldName) {
    				case "title" :
    					if (ascending)
    						orders.add(ArtObject.SORT.TITLE_ASC);
    					else
    						orders.add(ArtObject.SORT.TITLE_DESC);
    					break;
    				case "number" :
    					if (ascending)
    						orders.add(ArtObject.SORT.ACCESSIONNUM_ASC);
    					else
    						orders.add(ArtObject.SORT.ACCESSIONNUM_DESC);
    					break;
    				case "artistNames" :
    					if (ascending)
    						orders.add(ArtObject.SORT.FIRST_ARTIST_ASC);
    					else
    						orders.add(ArtObject.SORT.FIRST_ARTIST_DESC);
    					break;
    				case "lastModified" :
    					if (ascending)
    						orders.add(ArtObject.SORT.LASTDETECTEDMODIFICATION_ASC);
    					else
    						orders.add(ArtObject.SORT.LASTDETECTEDMODIFICATION_DESC);
    					break;
    				case "id" :
    					if (ascending)
    						orders.add(ArtObject.SORT.OBJECTID_ASC);
    					else
    						orders.add(ArtObject.SORT.OBJECTID_DESC);
    					break;
    				default:
    					throw new APIUsageException("Sorting on field " + fieldName + " is unsupported.");
    				}
    			}
    		}
    	}
    	// always append the default sort order to the end of the list
		orders.add(defaultSortOrder);
		return new SortHelper<ArtObject>(orders.toArray());
    }
    
    private String processSource(HttpServletRequest req) throws APIUsageException {
    	String source = null;
    	Matcher m = sourceMatcher.matcher(req.getRequestURI());
    	if (m.find())
    		source = m.group(1);
    	if (source == null)
    		source = "tms";
    	if (!source.equals("tms"))
    		throw new APIUsageException("No such source: " + source);
    	return source;
    }

    /*
    // we can use this approach for "startswith" operator should we decide to switch to that
    private void processSuggestableField(SearchHelper<ArtObject> searchHelper, List<String> textValues1, List<String> textValues2, ArtObject.SEARCH field) {
    	List<String> aList = CollectionUtils.newArrayList(textValues1, textValues2);
    	aList = CollectionUtils.clearEmptyOrNull(aList);

    	List<String> nList = CollectionUtils.newArrayList();
    	String search=null;
    	for (String an : aList) {
    		if (!StringUtils.isNullOrEmpty(an)) {
    			nList.add(an);
    			if (search != null)
    				search += " ";
    			else
    				search = "";
    			search += an;
    		}
    	}
    	if (!StringUtils.isNullOrEmpty(search)) {
    		List<Suggestion> suggestions = CollectionUtils.newArrayList();
    		switch (field) {
    		case TITLE:
    			suggestions = artDataManager.suggestArtObjectsByTitle(search); 		
    			break;
    		case ARTIST_ALLNAMES:
    			suggestions = artDataManager.suggestArtObjectsByArtistName(search);
    			break;
    		default: 
    			break;
    		}
    		// the suggestions will have a list of object IDs, so we have to translate that into a list of IDs that are provided
    		// to the byID search
    		List<String> ids = CollectionUtils.newArrayList();
    		for (Suggestion s : suggestions) {
    			ids.add(s.getEntityID().toString());
    		}
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, SEARCH.OBJECTID, ids, false));
    	}
    }*/

    // ARTISTNAMES & TITLES FIELD
    private void processTextField(SearchHelper<ArtObject> searchHelper, List<String> textValues1, List<String> textValues2, ArtObject.SEARCH field) {
    	List<String> aList = CollectionUtils.newArrayList(textValues1, textValues2);
    	aList = CollectionUtils.clearEmptyOrNull(aList);

    	List<String> nList = CollectionUtils.newArrayList();
    	for (String an : aList) {
    		if (!StringUtils.isNullOrEmpty(an))
    			nList.add(an);
    	}
    	if (nList != null && nList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.LIKE, field, nList, true));
    }

	// ID FIELD
	public void processIDs(SearchHelper<ArtObject> searchHelper, List<String> ids, List<String> cultObj_ids) {
		List<String> iList = CollectionUtils.newArrayList(ids, cultObj_ids);
		iList = CollectionUtils.clearEmptyOrNull(iList);
    	if (iList != null && iList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, ArtObject.SEARCH.OBJECTID, iList));
    }
    
	// LASTMODIFIED FIELD
    public void processLastModified(SearchHelper<ArtObject> searchHelper, List<String> lastModified, List<String> cultObj_lastModified) throws APIUsageException {
    	List<String> lmList = CollectionUtils.newArrayList(lastModified, cultObj_lastModified);
    	
    	int size = ( lmList == null ? 0 : lmList.size() );
    	String lm1 = null;
    	if (size > 0)
    		lm1 = lmList.get(0);
    	String lm2 = null;
    	if (size > 1)
    		lm2 = lmList.get(1);
    	
    	// if one of the supplied values is not null, then we can proceed
    	if (!StringUtils.isNullOrEmpty(lm1) || !StringUtils.isNullOrEmpty(lm1)) {
    		// take the first two
    		try {
    			
    			/*  FROM API CONTROL DOC
    			 * 	If a single value is supplied, that value should be assumed as the earliest date with an unbounded upper limit.  
    			 *  When two or more values are supplied, only the first two values should be used and those values represent a date 
    			 *  range.  If the second value is earlier than the first, the values should be swapped by the API implementation in 
    			 *  order to construct a valid date range for the search.
    			 */
    			
    			// if lm1 is empty, then we will always assign a lower bound based on the TMS conversion date
    			if (StringUtils.isNullOrEmpty(lm1))
    				lm1 = "1/1/2008";
    			// if lm2 is empty, then we assign an upper bound of today's date since no records could possibly be modified after the current time
    			if (StringUtils.isNullOrEmpty(lm2))
    				lm2 = DateTime.now().toString();
    			
    			// swap if lm1 is > lm2 for some reason, then we swap values
    			if (lm1.compareTo(lm2) > 0) {
    				String hold = lm1; lm1=lm2; lm2=hold;
    			}

    			// now, all the dates should be set to something non empty, so we try to parse them
    			DateTime dm1 = new DateTime(lm1);
    			DateTime dm2 = new DateTime(lm2);
        		searchHelper.addFilter(new SearchFilter(SEARCHOP.BETWEEN, ArtObject.SEARCH.LASTDETECTEDMODIFICATION, dm1.toString(), dm2.toString() ));
    		}
    		catch (IllegalArgumentException ie) {
    			throw new APIUsageException("Could not parse one of the dates supplied for last modified date: "+ie.getMessage());
    		}
    	}
    }
    
    


}