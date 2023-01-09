/*
    NGA ART DATA API: ObjectSearchController maps API requests for searches of art object records 
    and coordinates the searches and responses 

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
package gov.nga.integration.cspace;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.common.entities.art.Derivative;
import gov.nga.common.entities.art.ArtObject.SORT;
import gov.nga.common.entities.art.Derivative.ImgSearchOpts;
import gov.nga.integration.controllers.RecordSearchController;
import gov.nga.integration.cspace.imageproviders.WebImage;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.search.SearchFilter;
import gov.nga.common.search.SearchHelper;
import gov.nga.common.search.SearchHelper.SEARCHOP;
import gov.nga.common.search.SortHelper;

import gov.nga.utils.CollectionUtils;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.imaging.NGAImage;
import gov.nga.common.utils.StringUtils;

@RestController
public class ObjectSearchController extends RecordSearchController {

	private static final Logger log = LoggerFactory.getLogger(ObjectSearchController.class);
	
	private static Pattern sourcePattern = Pattern.compile("/art/(.*)/objects");
	public Pattern getSourcePattern() {
		return sourcePattern;
	}
	
	private static String[] supportedNamespaces = new String[]{ObjectRecord.getDefaultNamespace()};

	private static String[] sources = new String[]{"tms"};
	public String[] getSupportedSources() {
		return sources;
	}
	
	public String getDefaultNamespace() {
		return ObjectRecord.getDefaultNamespace();
	}

	private static final SORT defaultSortOrder = ArtObject.SORT.ACCESSIONNUM_ASC;
	    
    @Autowired
    private ArtDataQuerier artDataQuerier;
    
    @Autowired
    private ArtDataManagerService artDataManager;
    
    @Autowired
    private CSpaceTestModeService ts;

    @Autowired
    private OperatingModeService om;

    @Autowired
    ImageSearchController imgCtrl;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String[].class, new StringArrayPropertyEditor(null));
    }
    
    @RequestMapping(value={"/art/objects.json","/art/{source}/objects.json"},method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<Items> objectRecordsSource (
    		
    		@RequestParam(value="id", 					required=false) String[] ids,
			@RequestParam(value="cultObj:id",			required=false) String[] cultObj_ids,
			
			@RequestParam(value="tmsStatus",			required=false) String[] statuses,
			@RequestParam(value="cultObj:tmsStatus",	required=false) String[] cultObj_statuses,	
			
			@RequestParam(value="lastModified", 		required=false) String[] lastModified,
			@RequestParam(value="cultObj:lastModified", required=false) String[] cultObj_lastModified,

			@RequestParam(value="artistNames", 			required=false) String[] artistNames,
			@RequestParam(value="cultObj:artistNames", 	required=false) String[] cultObj_artistNames,
			
			@RequestParam(value="title", 				required=false) String[] titles,
			@RequestParam(value="cultObj:title",		required=false) String[] cultObj_titles,

			@RequestParam(value="number", 				required=false) String[] numbers,
			@RequestParam(value="cultObj:number",		required=false) String[] cultObj_numbers,

			@RequestParam(value="onView", 				required=false) String[] onView,
			@RequestParam(value="cultObj:onView",		required=false) String[] cultObj_onView,

			@RequestParam(value="overviewText", 		required=false) String[] overviewText,
			@RequestParam(value="cultObj:overviewText",	required=false) String[] cultObj_overviewText,

			@RequestParam(value="hasOverviewText", 		required=false) String[] hasOverviewText,
			@RequestParam(value="cultObj:hasOverviewText",	required=false) String[] cultObj_hasOverviewText,
			
			@RequestParam(value="isExhibitionMember",	required=false) String[] exhIDS,
    		@RequestParam(value="cultObj:isExhibitionMember", required=false) String[] cultObj_exhIDS,
    		
    		@RequestParam(value="vbclassification",		required=false)	String[] vbclassification,
    		@RequestParam(value="cultObj:vbclassification", required=false) String[] cultObj_vbclassification,
			
			@RequestParam(value="references", 			required=false, defaultValue="true") boolean references,
			@RequestParam(value="thumbnails", 			required=false, defaultValue="true") boolean thumbnails,
			@RequestParam(value="base64", 				required=false, defaultValue="true") boolean base64,

			@RequestParam(value="order", 				required=false					   ) List<String> order,

			@RequestParam(value="skip",					required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",				required=false, defaultValue="50") int limit,
			
  /* 		NORMALLY, WE WOULD USE A PATH VARIABLE, BUT IT'S OPTIONAL AND SPRING DOESN'T SUPPORT THAT YET
   * 		SO WE HAVE TO PARSE THE REQUEST URI AND PULL IT OUT  		
			@PathVariable(value="source") String source,
  */
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {  	
    	
    	// getSource validates source if present or returns all supported sources, enabling us to merely invoke searches
    	// for all of the requested sources which, in this case, is just one of course (tms).
    	getSources(request);
    	
    	ResultsPaginator paginator = getPaginator(skip, limit);
    	log.info(String.format("ResultsPaginator(%d, %d) = %s", skip, limit, paginator));

    	// various helpers are used to accumulate search criteria, order, and paginate the results
    	SearchHelper<ArtObject> searchHelper = new SearchHelper<ArtObject>();

    	// process all of the request parameters
    	processIDs(searchHelper, ids, cultObj_ids);
    	// these can be used if we decide to go with a "startswith" type of search rather than "contains"
    	//processSuggestableField(searchHelper, artistNames, cultObj_artistNames, ArtObject.SEARCH.ARTIST_ALLNAMES);
    	//processSuggestableField(searchHelper, titles, cultObj_titles, ArtObject.SEARCH.TITLE);
    	processTextField(searchHelper, numbers, cultObj_numbers, ArtObject.SEARCH.ACCESSIONNUM);
    	processTextField(searchHelper, onView, cultObj_onView, ArtObject.SEARCH.ONVIEW);
    	processLastModified(searchHelper, lastModified, cultObj_lastModified);
    	processTextField(searchHelper, titles, cultObj_titles, ArtObject.SEARCH.TITLE);
    	processTextField(searchHelper, overviewText, cultObj_overviewText, ArtObject.SEARCH.OVERVIEW);
    	processTextField(searchHelper, hasOverviewText, cultObj_hasOverviewText, ArtObject.SEARCH.HASOVERVIEWTEXT);
    	processTextField(searchHelper, artistNames, cultObj_artistNames, ArtObject.SEARCH.ARTIST_ALLNAMES);
    	processVBClassificationField(searchHelper, vbclassification, cultObj_vbclassification);
    	final List<String> exhibitions = processExhibitionField(searchHelper, exhIDS, cultObj_exhIDS);
    	processTMSStatusField(searchHelper, statuses, cultObj_statuses);
    	
    	SortHelper<ArtObject> sortHelper = getSortHelper(order);
    	
    	// the list of items that will be returned (constructed from art object records)
		List<SearchResultItem> partialResults = CollectionUtils.newArrayList();

    	// execute the search using the prepared search helper, but only if we actually have search criteria
		// otherwise, we return an empty result set
		List<ArtObject> artObjects = CollectionUtils.newArrayList();
		if (searchHelper.getFilterSize() > 0)
		{
			log.info("Paginator before search: " + paginator);
			artObjects = artDataManager.getArtDataQuerier()
								.searchArtObjects(searchHelper, paginator, null, sortHelper)
								.getResults();
			log.info(String.format("Paginator after search %s[%d]", paginator, artObjects.size()));
		}
    	log.info(String.format("Num of filters: %d", searchHelper.getFilterSize()));
    	logSearchResults(statsRecorder, request, paginator.getTotalResults());
    	if (artObjects.size() > 0) {
    		
    		// we need to find all of the images associated with these objects, so we do that search ahead of time now
    		// rather than repeating a bunch of times for every item in our page of results - we'll store them in
    		// a map for easy access later
    		SearchHelper<CSpaceImage> imageSearchHelper = new SearchHelper<CSpaceImage>();
        	List<CSpaceImage> images = imgCtrl.searchImages(null, imageSearchHelper, artObjects);
        	Map<Long, List<CSpaceImage>> imagesMap = CollectionUtils.newHashMap();
        	for (CSpaceImage img : images) {
        		List<CSpaceImage> oList = imagesMap.get(img.getArtObjectID());
        		if (oList == null) {
        			oList = CollectionUtils.newArrayList();
        			imagesMap.put(img.getArtObjectID(), oList);
        		}
        		oList.add(img);
        	}
    		
    		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    		try {
    			// Map used to accumulate the thumbnail computations from Futures
    			Map<Long,Future<String>> thumbnailMap = CollectionUtils.newHashMap();
    			if (thumbnails) {
    				
    				if (ts.isTestModeOtherHalfObjects())
    					base64 = false;
    				
    				int thumbWidth = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailWidthProperty);
    				int thumbHeight = artDataManager.getConfig().getInteger(CSpaceConfigService.thumbnailHeightProperty);
    				// submit the work to fetch thumbnails and compute base64 values of them
    				for (ArtObject o : artObjects) {
    					NGAImage pImg = o.getPrimaryImage();
    					if (pImg != null) {
    						log.info(String.format("NGAImage: %s <hashcode> %d", pImg, pImg.hashCode()));
	    					Callable<String> thumbWorker = new ImageThumbnailWorker(pImg,thumbWidth,thumbHeight,base64);
							Future<String> future = threadPool.submit(thumbWorker);
							thumbnailMap.put(o.getObjectID(), future);
    					}
    				}
    			}
    			final Map<Long, List<ExhibitionArtObject>> exhibitionMap = CollectionUtils.newHashMap();
    			if (exhibitions.size() > 0)
    			{
    				final List<Long> exIDS = CollectionUtils.newArrayList();
    				for (String cand: exhibitions) {
    					try {
    						exIDS.add(Long.parseLong(cand));
    					}
    					catch (final Exception err) {
    						//do nothing
    					}
    				}
    				for (Exhibition exh: artDataQuerier.fetchByExhibitionIDs(exIDS).getResults()) {
    					exhibitionMap.put(exh.getID(), exh.getExhibitionObjects());
    				}
    			}
    			
    			for (ArtObject o : artObjects) {
    				AbridgedObjectRecord objectRecord = new AbridgedObjectRecord(
    						o, references, om, 
    						ts, imagesMap.get(o.getObjectID()), RecordSearchController.getRequestingServer(request),
    						exhibitionMap
    				);
    				Future<String> thumb = thumbnailMap.get(o.getObjectID());
    				String thumbVal = (thumb == null ? null : thumb.get());
    				partialResults.add(new SearchResultItem(thumbVal, objectRecord));
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
		log.info(String.format("Returning Response [rsltsize=%d,pnlimit=%d,pnskip=%d,pn=%s]",
				partialResults.size(), paginator.getStartIndex(),paginator.getPageSize(),paginator));
		return new ResponseEntity<Items>(new Items(paginator, partialResults), headers, HttpStatus.OK);
	}
    
    protected static ArtObject.SORT fieldNameToSortEnum(OrderField f) {
    	if (StringUtils.isBlank(f.fieldName))
    		return null;
    	switch (f.fieldName) {
    	case "cultObj:title" :
    		if (f.ascending)
    			return ArtObject.SORT.TITLE_ASC;
    		else
    			return ArtObject.SORT.TITLE_DESC;
    	case "cultObj:number":
    	case "cultObj:onView" :
    		if (f.ascending)
    			return ArtObject.SORT.ACCESSIONNUM_ASC;
    		else
    			return ArtObject.SORT.ACCESSIONNUM_DESC;
    	case "cultObj:artistNames" :
    		if (f.ascending)
    			return ArtObject.SORT.FIRST_ARTIST_ASC;
    		else
    			return ArtObject.SORT.FIRST_ARTIST_DESC;
    	case "cultObj:lastModified" :
    		if (f.ascending)
    			return ArtObject.SORT.LASTDETECTEDMODIFICATION_ASC;
    		else
    			return ArtObject.SORT.LASTDETECTEDMODIFICATION_DESC;
    	case "cultObj:id" :
    		if (f.ascending)
    			return ArtObject.SORT.OBJECTID_ASC;
    		else
    			return ArtObject.SORT.OBJECTID_DESC;
    	case "cultObj:tmsStatus":
    		if (f.ascending)
    			return ArtObject.SORT.TMS_STATUS_ASC;
    		else
    			return ArtObject.SORT.TMS_STATUS_DESC;
    	}
    	
    	return null;
    }
    
    
    // Spring REST will automatically parse values that are separated with a comma into an array
    // and will pass the individual values
    protected SortHelper<ArtObject> getSortHelper(List<String> order) throws APIUsageException {
    	List<ArtObject.SORT> orders = CollectionUtils.newArrayList();
    	for (OrderField f : getSortFields(order,supportedNamespaces)) {
    		ArtObject.SORT s = fieldNameToSortEnum(f);
    		if (s != null)
    			orders.add(s);
    		else
    			throw new APIUsageException("Sorting on field " + f.fieldName + " is unsupported.");
    	}
    	// always append the default sort order to the end of the list
    	orders.add(defaultSortOrder);
    	return new SortHelper<ArtObject>(orders.toArray(new Enum<?>[] {}));
    }
    
    // ARTISTNAMES & TITLES FIELD
    protected static void processTextField(SearchHelper<ArtObject> searchHelper, String[] textValues1, String[] textValues2, ArtObject.SEARCH field) {
    	List<String> aList = CollectionUtils.newArrayList(textValues1, textValues2);
    	aList = CollectionUtils.clearEmptyOrNull(aList);

    	List<String> nList = CollectionUtils.newArrayList();
    	for (String an : aList) {
    		if (StringUtils.isNotBlank(an))
    			nList.add(an);
    	}
    	if (nList != null && nList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.LIKE, field, nList, true));
    }
    
    private static List<String> uncoupleCommaStrings(final List<String> source)
    {
    	final List<String> list = CollectionUtils.newArrayList();
    	if (source != null)
    	{
    		for (String cand: source)
    		{
    			if (StringUtils.isNotBlank(cand))
    			{
    				for (String val: cand.split(","))
    				{
    					list.add(val);
    				}
    			}
    		}
    	}
    	return list;
    }
    
    //Status
    protected static void processTMSStatusField(final SearchHelper<ArtObject> searchHelper, final String[] idValues1, final String[] idValues2) {
    	final List<String> aList = uncoupleCommaStrings(
    				CollectionUtils.clearEmptyOrNull(CollectionUtils.newArrayList(idValues1, idValues2)));
    	
    	if (aList.size() > 0)
    	{
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, ArtObject.SEARCH.TMSSTATUS, aList));
    	}
    }
    
    //Exhibitions
    protected static List<String> processExhibitionField(final SearchHelper<ArtObject> searchHelper, final String[] idValues1, final String[] idValues2) {
    	final List<String> aList = CollectionUtils.clearEmptyOrNull(CollectionUtils.newArrayList(idValues1, idValues2));
    	
    	if (aList.size() > 0)
    	{
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, ArtObject.SEARCH.ISEXHIBITIONMEMBER, aList));
    	}
    	return aList;
    }
    
    //VB Classification
    protected static void processVBClassificationField(final SearchHelper<ArtObject> searchHelper, final String[] vbc, final String[] vbc2) {
    	final List<String> aList = uncoupleCommaStrings(
    				CollectionUtils.clearEmptyOrNull(CollectionUtils.newArrayList(vbc, vbc2)));
    	if (aList.size() > 0)
    	{
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, ArtObject.SEARCH.VISUALBROWSERCLASSIFICATION, aList));
    	}
    }

	// ID FIELD
	protected static void processIDs(SearchHelper<ArtObject> searchHelper, String[] ids, String[] cultObj_ids) {
		List<String> iList = uncoupleCommaStrings(CollectionUtils.newArrayList(ids, cultObj_ids));
		iList = CollectionUtils.clearEmptyOrNull(iList);
    	if (iList != null && iList.size() > 0)
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.EQUALS, ArtObject.SEARCH.OBJECTID, iList));
    	log.info(String.format("Processing ids: %s + %s = %s", ids, cultObj_ids, iList));
    }
    
	// LASTMODIFIED FIELD
    protected static void processLastModified(SearchHelper<ArtObject> searchHelper, String[] lastModified, String[] cultObj_lastModified) throws APIUsageException {
    	DateTime[] dates = getLastModifiedDates(lastModified,cultObj_lastModified);
    	if (dates != null && dates.length > 1) {
    		searchHelper.addFilter(new SearchFilter(SEARCHOP.BETWEEN, ArtObject.SEARCH.LASTDETECTEDMODIFICATION, dates[0].toString(), dates[1].toString() ));
    	}
    }
    
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
