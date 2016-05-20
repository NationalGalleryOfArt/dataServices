package gov.nga.integration.cspace;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObject.SORT;
import gov.nga.entities.art.Derivative.IMGVIEWTYPE;
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

	private static String[] sources = new String[]{"web-images-repository"};
	public String[] getSupportedSources() {
		return sources;
	}
	
	public String getDefaultNamespace() {
		return ImageRecord.getDefaultNamespace();
	}

	private static final SORT defaultSortOrder = ArtObject.SORT.ACCESSIONNUM_ASC;

    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping(value={"/media/images.json","/media/{source}/images.json"})
    public ResponseEntity<Items> imageRecordsSource (
    		
    		@RequestParam(value="id", 					required=false) List<String> ids,
			@RequestParam(value="image:id",				required=false) List<String> image_ids,

			@RequestParam(value="lastModified", 		required=false) List<String> lastModified,
			@RequestParam(value="image:lastModified", 	required=false) List<String> image_lastModified,

			@RequestParam(value="cultObj:id",			required=false) List<String> cultObj_ids,
			@RequestParam(value="cultObj:artistNames", 	required=false) List<String> cultObj_artistNames,
			@RequestParam(value="cultObj:title",		required=false) List<String> cultObj_titles,
			@RequestParam(value="cultObj:number",		required=false) List<String> cultObj_numbers,

			@RequestParam(value="references", 			required=false, defaultValue="true") boolean references,
			@RequestParam(value="thumbnails", 			required=false, defaultValue="true") boolean thumbnails,
			@RequestParam(value="base64", 				required=false, defaultValue="true") boolean base64,

			@RequestParam(value="order", 				required=false					   ) List<String> order,

			@RequestParam(value="skip",					required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",				required=false, defaultValue="50") int limit,
			
			HttpServletRequest request,
			HttpServletResponse response
	) throws APIUsageException, InterruptedException, ExecutionException {  	

    	// validate source if present
    	String[] requestedSources = getSources(request);
    	if (requestedSources.length > 1)
    		throw new APIUsageException("Multiple sources specified, but only one supported");
    	
    	// various helpers are used to accumulate search criteria, order, and paginate the results
    	SearchHelper<ArtObject> aoSearchHelper = new SearchHelper<ArtObject>();

    	// process all of the request parameters
    	// use the object search controller's static methods to help us process the art object fields 
    	ObjectSearchController.processIDs(aoSearchHelper, cultObj_ids, null);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_numbers, null, ArtObject.SEARCH.ACCESSIONNUM);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_titles, null, ArtObject.SEARCH.TITLE);
    	ObjectSearchController.processTextField(aoSearchHelper, cultObj_artistNames, null, ArtObject.SEARCH.ARTIST_ALLNAMES);
    	
    	SearchHelper<Derivative> searchHelper = new SearchHelper<Derivative>();
    	processIDs(searchHelper, ids, image_ids);
    	processLastModified(searchHelper, lastModified, image_lastModified);

    	List<Derivative> images = CollectionUtils.newArrayList();
    	// fetch all of the art objects matching the query and order those objects
    	// according to the specified sort order
    	Collection<ArtObject> artObjects = CollectionUtils.newArrayList();
    	if (aoSearchHelper.getFilterSize() > 0)
    		// execute the search using the prepared search helper if we have art object filter parameters
    		// or consider all derivatives for objects to be in play for other criteria
    		// no paginator since we want all of the results back
    		artObjects = artDataManager.searchArtObjects(aoSearchHelper, null, null);
    	
    	// since user selected no art object filters, they must have selected derivative filters or they're not going to get
    	// any results back because the result set would be enormously large which is just kind of silly, especially since we have
    	// to sort it
    	else if (searchHelper.getFilterSize() > 0)
    		artObjects = artDataManager.getArtObjectsRaw().values();

    	// add the largest images of each type to the results
    	if (!artObjects.isEmpty()) {
    		// loop through the list of images once, creating our initial list of Derivatives
    		for (ArtObject ao : artObjects) {
    			// add all images from this object to the list of Derivatives
    			images.addAll(ao.getLargestImages(IMGVIEWTYPE.allViewTypesExcept(IMGVIEWTYPE.CROPPED)));
    		}
    	}
   
    	/************************************************************************************************************************************************
               	 loop through each art object sort order individually and store a map of {ArtObject.SORT}{objectID} with value of the index in the list
               	 so that we can later use that ordering to sort the images once all images from all sources have been collected
               	Map<ArtObject.SORT, Map<Long, Long>> artObjectImageOrderIndexes = CollectionUtils.newHashMap();
    	   		for (Object soc : objectsSortHelper.getSortOrder()) {
    	   			Map<Long, Long> sortIndexes = CollectionUtils.newHashMap();
    	   			ArtObject.SORT os = (ArtObject.SORT) soc;
    	   			SortHelper<ArtObject> sh = new SortHelper<ArtObject>(os);
    	   			sh.sortEntities(artObjects);
    	   			long i = 0;
    	   			for (ArtObject ao : artObjects) {
    	   				sortIndexes.put(ao.getObjectID(), i++);
    	   			}
    	   			artObjectImageOrderIndexes.put(os, sortIndexes);
    	   		}
  	
    	// so here's how we can sort all derivatives by a mix of art object and derivative criteria
    	// loop through all of the sort criteria and sort the list by each criteria separately
    	// storing the order in a map{objectid,sort} = index in list
    	// so that way, we can always look up the object id to perform the proper sort criteria for the derivative
    	// and we assemble the candidate list of master images based on the returned object ids
    	// and looking only for the PTIF source files
    	
    	// then, if we have specific derivative search criteria such as IDs and last modified dates
    	// we process those against the (hopefully smaller) list of PTIF images, getting a list of matching 
    	// derivatives passing the derivatives search criteria
    	// and finally, we sort - we'll need a custom sorter in this class that draws upon the 
    	// derivatives.matchesAspect() and the map{objectid,sort} scores from above
    	// that should work fairly well
    	
    	// finally, when we have multiple sources of derivatives, we're going to
    	// have to perform a search by visiting all image sources with a potentially large
    	// list of object IDs.  So, that can be assisted by creating and indexing a temp table
    	// of object IDs and then joining against it - so, that'll work and it will allow a pattern
    	// to be created for additional sources
    	// spring can help us to find implementations of interfaces as follows: 
    	
    	BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
    	ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

    	TypeFilter tf = new AssignableTypeFilter(CLASS_YOU_WANT.class);
    	s.addIncludeFilter(tf);
    	s.scan("package.you.want1", "package.you.want2");       
    	String[] beans = bdr.getBeanDefinitionNames();
    	
    	
    	// this class will visit all implementers, passing a potentially large list of matching
    	// objects as well as the remaining search criteria for derivative metadata (lastModified, imageIDs)
   
    	// so the interface would return lists of derivatives (or perhaps a more advanced class of image constructed from derivatives)
    	// and would accept the list of object IDs and derivative search criteria - it would also have to implemented
    	// certain searchable interfaces to provide matches filter so we can sort them consistently
    	// and that's about it - this class would provide the searches since object criteria are also necessary
    	**********************************************************************************************************************/
    	
    	// visit all implementers as described above, accumulating images as necessary - for now we only have a single 
    	// implementation which is ArtObject Derivatives, and we've already added those by the time we get here - now 
    	// it's time to filter on any derivative criteria such as image ID and last modified dates

    	// get the sort helper for derivatives and a paginator and execute the search
    	SortHelper<Derivative> sortHelper = getSortHelper(order);
    	ResultsPaginator paginator = getPaginator(skip, limit);
    	images = searchHelper.search(images, paginator, null, sortHelper);
    	
    	// the list of items that will be returned (constructed from art object records)
		List<Item> partialResults = CollectionUtils.newArrayList();
    	
		ErrorLoggerController.logSearchResults(request, images.size());
    	if (images.size() > 0) {
    		
    		int thumbWidth = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailWidthProperty);
			int thumbHeight = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailWidthProperty);

    		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    		try {
    			// Map used to accumulate the thumbnail computations from Futures
    			Map<Object,Future<String>> thumbnailMap = CollectionUtils.newHashMap();
    			if (thumbnails) {
    				// submit the work to fetch thumbnails and compute base64 values of them
    				for (Derivative d : images) {
    					Callable<String> thumbWorker = new ImageThumbnailWorker(d,thumbWidth,thumbHeight,base64);
    					Future<String> future = threadPool.submit(thumbWorker);
    					thumbnailMap.put(d, future);
    				}
    			}
    			for (Derivative d : images) {
    				URL imageURL = null;
    				String scheme = RecordSearchController.getRequestScheme(request);
    				try {
    					imageURL = new URL(scheme,request.getServerName(),request.getServerPort(),"/media/"+d.getSource()+"/images/"+d.getImageID()+".json");
    				}
    				catch (MalformedURLException me) {
    					log.error("Problem creating image URL: " + me.getMessage());
    				}
    				Record imageRecord = new AbridgedImageRecord(d, references);
    				Future<String> thumb = thumbnailMap.get(d);
    				String thumbVal = (thumb == null ? null : thumb.get());
    				partialResults.add(new Item(imageURL, thumbVal, imageRecord));
    			}
    		}
    		finally {
    			threadPool.shutdown();
    		}
    	}
    	
    	//log.info("thumbnail cache size: " + ArtObjectThumbnailWorker.getCache().size());
		// validate the request and response accordingly if there are problems
		// assemble a well formed response
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<Items>(new Items(paginator, partialResults), headers, HttpStatus.OK);
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
    	case "derivative:id" :
    		if (f.ascending)
    			return Derivative.SORT.IMAGEID_ASC;
    		else
    			return Derivative.SORT.IMAGEID_DESC;
    	}
    	return null;
    }

    // Spring REST will automatically parse values that are separated with a comma into an array
    // and will pass the individual values
    protected SortHelper<Derivative> getSortHelper(List<String> order) throws APIUsageException {
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
    	return new SortHelper<Derivative>(orders.toArray());
    }

	// ID FIELD
	public static void processIDs(SearchHelper<Derivative> searchHelper, List<String> ids, List<String> image_ids) {
		List<String> iList = CollectionUtils.newArrayList(ids, image_ids);
		iList = CollectionUtils.clearEmptyOrNull(iList);
    	if (iList != null && iList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, Derivative.SEARCH.IMAGEID, iList));
    }
    
	// LASTMODIFIED FIELD
    protected static void processLastModified(SearchHelper<Derivative> searchHelper, List<String> lastModified, List<String> image_lastModified) throws APIUsageException {
    	DateTime[] dates = getLastModifiedDates(lastModified,image_lastModified,"1/1/2008");
    	if (dates != null && dates.length > 1) {
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.BETWEEN, Derivative.SEARCH.CATALOGUED, dates[0].toString(), dates[1].toString() ));
    	}
    }
    
}