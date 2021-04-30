package gov.nga.integration.controllers;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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

import gov.nga.common.utils.CollectionUtils;
import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtDataSuggestion;
import gov.nga.integration.records.Items;
import gov.nga.integration.records.suggestion.SuggestResultItem;
import gov.nga.common.search.ResultsPaginator;

@RestController
public class SuggestionController extends RecordSearchController  {
	
	private static Pattern sourcePattern = Pattern.compile("/art/(.*)/suggestions");
	private static final Logger log = LoggerFactory.getLogger(SuggestionController.class);
	public Pattern getSourcePattern() {
		return sourcePattern;
	}

    @Autowired
    private ArtDataManagerService artDataManager;
    
    @Autowired
    private ArtDataQuerier artDataQuerier;
    

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String[].class, new StringArrayPropertyEditor(null));
    }
    
    private ResponseEntity<Items<SuggestResultItem>>  sendSuggestions(final List<ArtDataSuggestion> passedSuggestions,
    							final int skip, final int limit,
    							final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    	ResponseEntity<Items<SuggestResultItem>> respEntity = null;
    	log.info("Suggestion request");
    	// getSource validates source if present or returns all supported sources, enabling us to merely invoke searches
    	// for all of the requested sources which, in this case, is just one of course (tms).
    	getSources(request);
    	
    	ResultsPaginator paginator = getPaginator(skip, limit);
    	paginator.setTotalResults(passedSuggestions.size());
    	final List<ArtDataSuggestion> suggestions = CollectionUtils.newArrayList(passedSuggestions.subList(paginator.getStartIndex(), paginator.getEndIndex()));
    	
    	final List<SuggestResultItem> suggestItems = CollectionUtils.newArrayList();
    	for (ArtDataSuggestion suggestion: suggestions) {
    		suggestItems.add(new SuggestResultItem(suggestion));
    	}
    	
    	logSearchResults(request, paginator.getTotalResults());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		respEntity = new ResponseEntity<Items<SuggestResultItem>>(new Items<SuggestResultItem>(paginator, suggestItems), headers, HttpStatus.OK);
    	return respEntity;
    }
    
    @RequestMapping(value={"/art/suggestions/works.json","/art/{source}/suggestions/works.json"},method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<Items<SuggestResultItem>> objectSuggestionssSource (
    		
    		@RequestParam(value="title", 					required=false) String title,
    		@RequestParam(value="artist",					required=false) String artist,
			@RequestParam(value="skip",						required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",					required=false, defaultValue="50") int limit,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	if (StringUtils.isNotBlank(artist)) { 
    		return sendSuggestions(artDataQuerier.suggestArtObjectFromArtist(artist, title).getResults(),
    								skip, limit, request, response);
    	}
    	return sendSuggestions(artDataQuerier.suggestArtObjects(title).getResults(),
									skip, limit, request, response);
    }

    
    @RequestMapping(value={"/art/suggestions/artists.json","/art/{source}/suggestions/artists.json"},method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<Items<SuggestResultItem>> artistsSuggestionssSource (
    		
    		@RequestParam(value="title", 					required=true) String title,
			@RequestParam(value="skip",						required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",					required=false, defaultValue="50") int limit,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	return sendSuggestions(artDataQuerier.suggestArtists(title).getResults(),
				skip, limit, request, response);
    }

    
    @RequestMapping(value={"/art/suggestions/exhibitions.json","/art/{source}/suggestions/exhibitions.json"},method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<Items<SuggestResultItem>> exhibitionsSuggestionssSource (
    		
    		@RequestParam(value="title", 					required=true) String title,
			@RequestParam(value="skip",						required=false, defaultValue="0") int skip,
			@RequestParam(value="limit",					required=false, defaultValue="50") int limit,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	return sendSuggestions(artDataQuerier.suggestExhibitions(title).getResults(),
				skip, limit, request, response);
    }
    
    
	private static String[] sources = new String[]{"tms"};
	public String[] getSupportedSources() {
		return sources;
	}
	public String getDefaultNamespace() {
		return "ngaSugg:";
	}
	
}
