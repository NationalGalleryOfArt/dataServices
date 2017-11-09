package gov.nga.integration.cspace;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObject.SORT;
import gov.nga.entities.art.Derivative;
import gov.nga.search.ResultsPaginator;
import gov.nga.search.SearchFilter;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.search.SortHelper;
import gov.nga.utils.CollectionUtils;

import gov.nga.utils.StringUtils;


@RestController
public class ImageSearchController extends RecordSearchController {

	private static final Logger log = LoggerFactory.getLogger(ImageSearchController.class);
	
	private static Pattern sourcePattern = Pattern.compile("/media/(.*)/images");
	public Pattern getSourcePattern() {
		return sourcePattern;
	}
	
	private static String[] supportedNamespaces = new String[]{
			ImageRecord.getDefaultNamespace(),
			ObjectRecord.getDefaultNamespace()
	};

	public String[] getSupportedSources() {
		List<String> sources = CollectionUtils.newArrayList();
    	Map<String, ImageSearchProvider> myBeans = appContext.getBeansOfType(ImageSearchProvider.class);
    	for (ImageSearchProvider isp : myBeans.values()) {
    		sources.addAll(Arrays.asList(isp.getProvidedSources()));
    	}
		return sources.toArray(new String[0]);
	}
	
	public String getDefaultNamespace() {
		return ImageRecord.getDefaultNamespace();
	}

	private static final SORT defaultSortOrder = ArtObject.SORT.ACCESSIONNUM_ASC;

    @Autowired
    private ArtDataManagerService artDataManager;
    
    @Autowired
    private ApplicationContext appContext;
    
    @Autowired 
    private CSpaceConfigService cs;
    
    @Autowired
    private CSpaceTestModeService ts;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String[].class, new StringArrayPropertyEditor(null));
    }

    @RequestMapping(value={"/media/images.json","/media/{source}/images.json"})
    public ResponseEntity<Items> imageRecordsSource (
    		
    		@RequestParam(value="id", 					required=false) String[] ids,
			@RequestParam(value="image:id",				required=false) String[] image_ids,

			@RequestParam(value="lastModified", 		required=false) String[] lastModified,
			@RequestParam(value="image:lastModified", 	required=false) String[] image_lastModified,

			@RequestParam(value="cultObj:id",			required=false) String[] cultObj_ids,
			@RequestParam(value="cultObj:artistNames", 	required=false) String[] cultObj_artistNames,
			@RequestParam(value="cultObj:title",		required=false) String[] cultObj_titles,
			@RequestParam(value="cultObj:number",		required=false) String[] cultObj_numbers,

			@RequestParam(value="references", 			required=false, defaultValue="true") boolean references,
			@RequestParam(value="thumbnails", 			required=false, defaultValue="true") boolean thumbnails,
			@RequestParam(value="base64", 				required=false, defaultValue="true") boolean base64,

			@RequestParam(value="order", 				required=false					   ) List<String> order,

			@RequestParam(value="skip",					required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",				required=false, defaultValue="50") int limit,
			
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {  	

		int thumbWidth = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailWidthProperty);
		int thumbHeight = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailWidthProperty);

    	// validate source if it's present - most of the time it will not be present, but if it is, it must be handled properly
		// to restrict the search to a particular source of images
    	String[] sourceScope = getSources(request);
    	// various helpers are used to accumulate search criteria, order, and paginate the results
    	SearchHelper<ArtObject> aoSearchHelper = new SearchHelper<ArtObject>();

    	// process all of the request parameters
    	// use the object search controller's static methods to help us process the art object fields 
    	ObjectSearchController.processIDs(aoSearchHelper, cultObj_ids, null);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_numbers, null, ArtObject.SEARCH.ACCESSIONNUM);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_titles, null, ArtObject.SEARCH.TITLE);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_artistNames, null, ArtObject.SEARCH.ARTIST_ALLNAMES);

    	SearchHelper<CSpaceImage> imageSearchHelper = new SearchHelper<CSpaceImage>();
    	processIDs(imageSearchHelper, ids, image_ids);
    	processLastModified(imageSearchHelper, lastModified, image_lastModified);
    	
    	ResultsPaginator paginator = getPaginator(skip, limit);
    	// the list of items that will be returned (constructed from art object records)
		List<Item> resultPage = CollectionUtils.newArrayList();
		HttpHeaders headers = new HttpHeaders();

    	// fetch all of the art objects matching the query and order those objects
    	// according to the specified sort order
    	List<ArtObject> artObjects = null;
    	if (aoSearchHelper.getFilterSize() > 0)
    		// execute the search using the prepared search helper if we have art object filter parameters
    		// or consider all images for objects to be in play for other criteria
    		// no paginator since we want all of the results back
    		artObjects = artDataManager.searchArtObjects(aoSearchHelper, null, null);
    	// since user selected no art object filters, they must have selected image filters or they're not going to get
    	// any results back because the result set would be enormously large which is just kind of silly, especially since we have
    	// to sort it
    	// if no criteria are sent, then return the empty set
    	else if (imageSearchHelper.getFilterSize() <= 0)
    		return new ResponseEntity<Items>(new Items(paginator, resultPage), headers, HttpStatus.OK);

    	List<CSpaceImage> images = searchImages(sourceScope, imageSearchHelper, artObjects);
		logSearchResults(request, images.size());
		
		// now that we have the full list of images, but before we grab thumbnails and assemble the results, we have
		// to sort and then clip the results as per the paging specified by the caller
    	SortHelper<CSpaceImage> sortHelper = getSortHelper(order);
    	// now, sort all of the images that resulted from the search, then clip the results as per skip and limit
    	sortHelper.sortEntities(images);
    	images = new SearchHelper<CSpaceImage>().clipToPage(images, paginator);

		// generate thumbnails and accumulate the results
    	if (images.size() > 0) {

    		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    		try {
    			// Map used to accumulate the thumbnail computations from Futures
    			Map<Object,Future<String>> thumbnailMap = CollectionUtils.newHashMap();
    			if (thumbnails) {
    				
    				if (cs.isTestModeOtherHalfObjects())
    					base64 = false;
    				
    				// submit the work to fetch thumbnails and compute base64 values of them
    				for (CSpaceImage d : images) {
    					Callable<String> thumbWorker = new ImageThumbnailWorker(d,thumbWidth,thumbHeight,base64);
    					Future<String> future = threadPool.submit(thumbWorker);
    					thumbnailMap.put(d, future);
    				}
    			}
    			for (CSpaceImage d : images) {
    				URL imageURL = null;
    				String[] parts = RecordSearchController.getRequestingServer(request);
    				try {
    					if (parts[2] != null)
    						imageURL = new URL(parts[0], parts[1], Integer.parseInt(parts[2]),"/media/"+d.getSource()+"/images/"+d.getImageID()+".json");
    					else
    						imageURL = new URL(parts[0], parts[1], "/media/"+d.getSource()+"/images/"+d.getImageID()+".json");
    				}
    				catch (MalformedURLException me) {
    					log.error("Problem creating image URL: " + me.getMessage());
    				}
    				Record imageRecord = new AbridgedImageRecord(d, references, ts);
    				Future<String> thumb = thumbnailMap.get(d);
    				String thumbVal = (thumb == null ? null : thumb.get());
    				resultPage.add(new Item(imageURL, thumbVal, imageRecord));
    			}
    		}
    		finally {
    			threadPool.shutdown();
    		}
    	}
    	
    	//log.info("thumbnail cache size: " + ArtObjectThumbnailWorker.getCache().size());
		// validate the request and response accordingly if there are problems
		// assemble a well formed response
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<Items>(new Items(paginator, resultPage), headers, HttpStatus.OK);
	}
    
    public List<CSpaceImage> searchImages(String [] sourceScope, SearchHelper<CSpaceImage> imageSearchHelper, List<ArtObject> artObjects) throws Exception { 
    	List<CSpaceImage> images = CollectionUtils.newArrayList();
    	
    	if (sourceScope == null)
    		sourceScope = getSupportedSources();
    	// TODO - opportunity exists to multi-thread this search
    	// these beans will have already been instantiated and are available for performing services
    	Map<String, ImageSearchProvider> myBeans = appContext.getBeansOfType(ImageSearchProvider.class);
//    	myBeans.forEach((k,v)->log.info(k + ":" + v.getClass().getName()));
    	for (ImageSearchProvider isp : myBeans.values()) {
    		Collection<CSpaceImage> imageSubSet = null;
    		if ( isp.providesSource(sourceScope) )
    			imageSubSet = isp.searchImages(imageSearchHelper, artObjects);
    		if (imageSubSet != null && imageSubSet.size() > 0)
    			images.addAll(imageSubSet);
    	}
    	return images;
    }
    
    protected static Object fieldNameToSortEnum(OrderField f) {
    	if (StringUtils.isNullOrEmpty(f.fieldName))
    		return null;
    	switch (f.fieldName) {
    	case "image:lastModified" :
    		if (f.ascending)
    			return Derivative.SORT.CATALOGUED_ASC;
    		else
    			return Derivative.SORT.CATALOGUED_DESC;
    	case "image:id" :
    		if (f.ascending)
    			return Derivative.SORT.IMAGEID_ASC;
    		else
    			return Derivative.SORT.IMAGEID_DESC;
    	}
    	return null;
    }

    // Spring REST will automatically parse values that are separated with a comma into an array
    // and will pass the individual values
    protected SortHelper<CSpaceImage> getSortHelper(List<String> order) throws APIUsageException {
    	List<Object> orders = CollectionUtils.newArrayList();
    	for (OrderField f : getSortFields(order,supportedNamespaces)) {
    		Object s = fieldNameToSortEnum(f);
    		if (s == null)
    			s = ObjectSearchController.fieldNameToSortEnum(f);
    		if (s != null)
    			orders.add(s);
    		else
    			throw new APIUsageException("Sorting on field " + f.fieldName + " is unsupported.");
    	}
    	// always append the default sort order to the end of the list
    	orders.add(defaultSortOrder);
    	return new SortHelper<CSpaceImage>(orders.toArray());
    }

	// ID FIELD
	public static void processIDs(SearchHelper<CSpaceImage> searchHelper, String[] ids, String[] image_ids) {
		List<String> iList = CollectionUtils.newArrayList(ids, image_ids);
		iList = CollectionUtils.clearEmptyOrNull(iList);
    	if (iList != null && iList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, Derivative.SEARCH.IMAGEID, iList));
    }
    
	// LASTMODIFIED FIELD
    protected static void processLastModified(SearchHelper<CSpaceImage> searchHelper, String[] lastModified, String[] image_lastModified) throws APIUsageException {
    	DateTime[] dates = getLastModifiedDates(lastModified,image_lastModified);
    	if (dates != null && dates.length > 1) {
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.BETWEEN, Derivative.SEARCH.CATALOGUED, dates[0].toString(), dates[1].toString() ));
    	}
    }

}