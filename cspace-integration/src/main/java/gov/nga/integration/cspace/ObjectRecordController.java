/*
    NGA ART DATA API: ObjectRecordController maps API requests for art object records 
    and coordinates the responses 

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

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.entities.art.OperatingModeService;
import gov.nga.integration.controllers.RecordSearchController;
import gov.nga.integration.cspace.monitoring.GrpcTMSStats;
import gov.nga.common.search.SearchHelper;
import gov.nga.utils.LongUtils;

@RestController
public class ObjectRecordController {

	private static final Logger log = LoggerFactory.getLogger(ObjectRecordController.class);
    
    @Autowired
    private ArtDataManagerService artDataManager;
    
    @Autowired
    private CSpaceTestModeService ts;

    @Autowired
    private OperatingModeService om;

	@Autowired
	private ImageSearchController imgCtrl;
	
	@Autowired
	protected GrpcTMSStats statsRecorder;

    @RequestMapping(value="/art/objects/{id}.json",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<RecordContainer> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	return objectRecordSource(null, id, request, response);
    }
    
    // TODO - log every request since we're not doing that right now - or since we're searching anyway, log in the search instead

    // TODO - if diacritics are included in the query, search only the diacritical forms rather than the non-diacritical forms
    @RequestMapping(value="/art/{source}/objects/{id}.json",method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public ResponseEntity<RecordContainer> objectRecordSource(
    		@PathVariable(value="source") String source,
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	
    	log.debug("SOURCE: " + source);
    	// not implemented if the source is not specified OR (preferably) a redirect to the generic search for object records
    	if (source == null) {
    		try {
    			return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(new URI(response.encodeRedirectURL("/art/objects.json?id="+id))).body(null);
    		}
    		catch (Exception ue) {
    			log.error("Unexpected exception with URI: " + ue.getMessage());
    			return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    		}
    	}
    	

    	// not found if the source != tms (at least for now)
    	if (!source.equals("tms"))
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	// validate that the ID conforms to NGA specifications - it must be parseable into a Long for it to be TMS compatible
    	Long l = LongUtils.stringToLong(id, null);
    	if (l == null)
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    	
    	// fetch the object using the art data manager service
    	// TODO this can throw an exception if the data is not ready in which case we should 1) be prepared to handle that exception and 2) respond with appropriate error
    	ArtObject o = artDataManager.getArtDataQuerier().fetchByObjectID(l).getResults().get(0);
    	if (o == null)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	// if we have gotten to this point, then we found the art object and can construct a response

    	// find any images that are associated with this art object
		SearchHelper<CSpaceImage> imageSearchHelper = new SearchHelper<CSpaceImage>();
    	List<CSpaceImage> images = imgCtrl.searchImages(null, imageSearchHelper, Arrays.asList(o));

    	// and construct the object record along with all of its references
    	ObjectRecord or = new ObjectRecord(o, artDataManager.getArtDataCacher().getLocationsMap(), om, ts, images, RecordSearchController.getRequestingServer(request));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		RecordSearchController.logSearchResults(statsRecorder, request, 1);
		 
		return new ResponseEntity<RecordContainer>(new RecordContainer(or), headers, HttpStatus.OK);
	}

    
}