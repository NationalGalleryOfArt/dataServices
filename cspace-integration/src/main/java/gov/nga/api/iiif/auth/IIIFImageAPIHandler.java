package gov.nga.api.iiif.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.integration.cspace.CSpaceImage;
import gov.nga.integration.cspace.imageproviders.WebImageSearchProvider;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.utils.ConfigService;

@RestController
public class IIIFImageAPIHandler {

	@Autowired
	ConfigService cs;

	@Autowired
	WebImageSearchProvider webImageSearchProvider;

	private static final Logger log = LoggerFactory.getLogger(IIIFImageAPIHandler.class);
	
	public static enum EXCLUDEHEADER {
		CACHE_CONTROL("Cache-Control"),
		EXPIRES("Expires"),
		TRANSFER_ENCODING("Transfer-Encoding"),
		KEEP_ALIVE("Keep-Alive");
		
		private String label=null;

		private EXCLUDEHEADER(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	/*	
	 *  handle requests for info.json separately from 

//    @Reques Mapping("/iiif/* *\ /i nfo. json") 
    public ResponseEntity<InfoJSON> objectRecordNoSource(
    		@PathVariable(value="id") String id,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception {
    	return imageRecordSource(null, id, request, response);
    }
	 */

	// handle all IIIF requests and proxy to IIP Image Server as necessary
	//                 /iiif/public/objects/1/1/3/8/1138-primary-0-nativeres.ptif/full/256,256/0/default.jpg
	// the image id will be what's in the content of **
	// can also do this remember: "/media/images.json","/media/{source}/images.json"
	//		value={	"/iiif/{permMode}/**/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}", 
	//				"/iiif/{permMode}/**/{imgFilename}/{region}/{size}/{rotation}/{quality}.maxsampling.{sampleSize}.{format}"} 

	@RequestMapping("/${ngaweb.imagingServerIIIFPublicPrefix}/{sampleSize}/**/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}")
	public ResponseEntity<InputStreamResource> iiifAuthHandler (
			@PathVariable(value="sampleSize") String sampleSize,
			@PathVariable(value="imgFilename") String imgFilename,
			@PathVariable(value="region") String region,
			@PathVariable(value="size") String size,
			@PathVariable(value="rotation") String rotation,
			@PathVariable(value="quality") String quality,
			@PathVariable(value="format") String format,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {

		try {

			String iiifPublicPrefix = cs.getString(IIIFAuthConfigs.iiifPublicPrefixPropertyName);
			String iiifPrivatePrefix = cs.getString(IIIFAuthConfigs.iiifPrivatePrefixPropertyName);

			Long samplingSize = null;
			// attempt to parse the sampleSize out of the quality if it's present
/*	    	Matcher m = permModeSamplingPAttern.matcher(permMode);
		    if (m.find()) {
		    	try {
		    		samplingSize = Long.parseLong(m.group(1));
		    		permMode = m.group(2);
		    	}
		    	catch (NumberFormatException nfe) {
		    		samplingSize = -1L;
		    	}
		    }
*/
			try {
				samplingSize = Long.parseLong(sampleSize);
			}
			catch (NumberFormatException nfe) {
				// then we interpret this as part of the path to the image rather than a sampling size
			}
			String imgVolPath = null;
			if (samplingSize != null)
				imgVolPath = request.getRequestURI().replace("/"+iiifPublicPrefix+"/"+sampleSize,"").replace("/"+imgFilename+"/"+region+"/"+size+"/"+rotation+"/"+quality+"."+format,"");
			else
				imgVolPath = request.getRequestURI().replace("/"+iiifPublicPrefix,"").replace("/"+imgFilename+"/"+region+"/"+size+"/"+rotation+"/"+quality+"."+format,"");

			log.debug("sampleSize: " + sampleSize );
			log.debug("samplingSize: " + samplingSize );
			log.debug("imageVolPath: " + imgVolPath);
			log.debug("imageFilename: " + imgFilename);
			log.debug("region: " + region );
			log.debug("size: " + size );
			log.debug("rotation: " + rotation);
			log.debug("quality: " + quality );
			log.debug("format: " + format );

			// now... we don't really want to have to parse anything in IIIF and change the requested resolutions, etc. if we don't have to do that.
			// MAX_CVT can serve its existing, performance focused, purpose and is independent of max sample sizes to be used in the underlying images 
			// What we need to do now is figure out whether the image being requested has any special rights constraints that limit the size
			// of the sample that should be used prior to generating the requested image size.  This will simply be a max width and a max height.  
			// We'll convey those to IIP via a custom HTTP HEADER and IIP will read them and make sure that the max tile size it uses considers this; we 
			// only need to do this (for now) if the request was received from outside the firewall - otherwise, we don't set the header and just pass

			// so, what we want to do here is check for the presence of the NGA_INTERNAL cookie - it will be set by Apache for all internal requests 
			// and unset otherwise so should be fairly reliable - if unset, then we have to validate the request - otherwise we skip the validation part 

			// if the requested format is not either jpg or png then return a 404
			IMGFORMAT imgFormat = IMGFORMAT.formatFromExtension(format);
			if ( imgFormat != Derivative.IMGFORMAT.JPEG && imgFormat != Derivative.IMGFORMAT.PNG )
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

			SearchHelper<CSpaceImage> dSearchHelper = new SearchHelper<CSpaceImage>();
			dSearchHelper.addFilter(Derivative.SEARCH.IMAGEVOLUMEPATH, SEARCHOP.EQUALS, imgVolPath+'/');
			dSearchHelper.addFilter(Derivative.SEARCH.IMAGEFILENAME, SEARCHOP.EQUALS, imgFilename);
			List<CSpaceImage> images = webImageSearchProvider.searchImages(dSearchHelper, null);

			// we should only ever have one image with the same volumepath and filename given the way we handle object images right now
			log.debug("found: " + images.size() + " images"); 
			if (images == null || images.size() != 1)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

			CSpaceImage d = images.get(0);

			// if the requested image isn't a zoom image for some reason, then return a 404 error since we can't return a IIIF image from a non-zoomable file
			if ( !d.isZoom() )
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

		    
/*	    	if ( samplingSize != null && samplingSize == -1L ) {
	    		// user specified a bogus sampling size earlier so set the requested sampling size to the max sampling size
	    		// for private access in order to trigger a redirect to the default URL 
	    		samplingSize = d.getMaxSamplingSizeInPixels(false)+1;
	    	}
*/
			Long maxSamplingSize = null;
			
			// when zero, was returning the full detail  of the image - also I should use a prefix rather than mangling the spec... 
			// probably use /degrade:250/ in the URL for the redirect only

			String redirectURL = null;

			// read header set by Apache to determine whether this is an internal or external request
			boolean ngainternal = request.getHeader("NGA_INTERNAL") != null;

			// acquire max sampling size from APIs
			maxSamplingSize = d.getMaxSamplingSizeInPixels(!ngainternal);
			// scenario image is 200 but max sampling is 700 - but request was for default so we just don't enforce the header that's all - no redirect

			// if we are not permitted to show this image, then return an unauthorized message
			if ( maxSamplingSize != null && maxSamplingSize <= 0 )
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

			// if the requested sampling size is greater than the max sampling size or there isn't a requested max size and the max size is greater than or equal to the actual width 
			// of the image then just redirect to the default and recalculate <-- endless redirect
			if ( samplingSize != null && ( ( maxSamplingSize != null && samplingSize > maxSamplingSize ) || samplingSize <= 0 || samplingSize >= d.getWidth() || samplingSize >= d.getHeight() ) ) { 
				redirectURL = String.format("/%s%s/%s/%s/%s/%s/%s.%s",iiifPublicPrefix, imgVolPath, imgFilename, region, size, rotation, quality, format);
				samplingSize = null;
				maxSamplingSize = null;
			}
			// no point sending the max sampling size header if the max is greater than the longest side of the image anyway
			if ( maxSamplingSize != null && ( maxSamplingSize >= d.getWidth() || maxSamplingSize >= d.getHeight() ) )
				maxSamplingSize = null;

			// if a sample size is specified in the URL already or there's a max sample size to enforce
			// then we might have to redirect to a different URL using a new sample size
			if ( samplingSize != null || maxSamplingSize != null ) {
				Long newSampleSize = null;

				// if max sample size is in play but no sample size was specified OR the sample size user requested is larger than the max permissible
				if ( maxSamplingSize != null && ( samplingSize == null || samplingSize > maxSamplingSize ) )
					newSampleSize = maxSamplingSize;

				// if the sample size requested is larger than the width or height of the zoom image, then constrain it to the larger of the two
				if ( samplingSize != null && ( samplingSize > d.getWidth() || samplingSize > d.getHeight() ) )
					newSampleSize = d.getWidth() > d.getWidth() ? d.getWidth() : d.getHeight();

					// and finally, if the new sample size is >= the width or height of the image, then no point in specifying maxsampling at all
					if ( newSampleSize != null && ( newSampleSize >= d.getWidth() || newSampleSize >= d.getHeight() ) )
						newSampleSize = null;

					// and finally if we have a new sample size to enforce, then set the redirect URL to the new sample size
					if ( newSampleSize != null )
						redirectURL = String.format("/%s/%d%s/%s/%s/%s/%s/%s.%s",iiifPublicPrefix, newSampleSize, imgVolPath, imgFilename, region, size, rotation, quality, format);
			}

			log.debug("redirecting to: " + redirectURL);

			// redirect using a temporary redirect if appropriate - use temporary since we don't want browsers to cache the redirect since the
			// response will be different depending on the conditions by which the request was made
			if ( redirectURL != null )
				return ResponseEntity.status(HttpStatus.SEE_OTHER).location(new URI(response.encodeRedirectURL(redirectURL))).body(null);

			// by default allow caching of everything
			boolean nocache = false;
			// but don't cache requests for default images (samplingSize == null) with public sampling restrictions that are smaller than the image's longest side
			Long maxPublicPix = d.getMaxSamplingSizeInPixels(true);
			if ( samplingSize == null && maxPublicPix != null && maxPublicPix < d.getLongestSideInPixels() )
				nocache = true;
			
			String serverURL = cs.getString(Derivative.imagingServerURLPropertyName);
			String proxyURL = String.format("https:%s/%s%s/%s/%s/%s/%s/%s.%s", serverURL, iiifPrivatePrefix, imgVolPath, imgFilename, region, size, rotation, quality, format);
			// if we have unlimited maxSamplingSize then we just proxy the request normally

			URI imageURI = new URI(proxyURL);

			log.debug("proxying to: " + imageURI.toString());
			
			HttpURLConnection urlConnection = (HttpURLConnection) imageURI.toURL().openConnection();
			// add a special HTTP header here to instruct IIP to only take up to a certain maximum tile size when resampling if we have such a restriction
			urlConnection.setUseCaches(false);
			if ( samplingSize != null )
				urlConnection.setRequestProperty("MAX_SAMPLE_SIZE", samplingSize.toString());
			urlConnection.connect();
			
			Map<String, List<String>> map = urlConnection.getHeaderFields();

			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				log.debug("Key : " + entry.getKey() + " ,Value : " + entry.getValue());

				String headerKey = entry.getKey();
				if (headerKey != null) {
					boolean copyToResponse = true;
					for (EXCLUDEHEADER eh : EXCLUDEHEADER.values() ) {
						if (headerKey.equals(eh.getLabel())) {
							// allow cache control headers through but only if we are permitting caching of the response
							if ( eh != EXCLUDEHEADER.CACHE_CONTROL || nocache )
								copyToResponse = false;
							break;
						}
					}
					if (copyToResponse) {
						// copy all of the relevant response headers from IIP directly to this response
						for (String s : entry.getValue())
							response.setHeader(entry.getKey(), s);
					}
				}
				
			}
			if ( nocache )
				response.setHeader(EXCLUDEHEADER.CACHE_CONTROL.getLabel(), "no-cache");
			
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(imgFormat.getMimetype()))
					.body(new InputStreamResource(urlConnection.getInputStream()));

			
			// TODO - need an info.json handler for this as per IIIF specs - but do we actually need this now or can it wait until
			// we have a real auth need vs. just external?
			/*URI info_json = new URI("https://media.nga.gov/iiif/public/objects/1/1/3/8/1138-primary-0-nativeres.ptif/info.json");
		Integer max_width=1024;
		Integer max_height=1024;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<InfoJSON>(new InfoJSON(info_json, max_width, max_height), headers, HttpStatus.OK);
			 */

		}
		// return 404 if the source file cannot be found or accessed
		catch (IOException ie) {
			log.warn("IO Exception trying to read IIIF Image from Image Server",ie);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		// 

		/*    	// if the source is not specified or too many sources are specified, then redirect to the generic search for image records service
    	String [] sourceScope = imgCtrl.getSources(request); 
    	if (sourceScope.length != 1) {
    		try {
    			return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(new URI(response.encodeRedirectURL("/media/images.json?id="+id))).body(null);
    		}
    		catch (Exception ue) {
    			log.error("Unexpected exception with URI: " + ue.getMessage());
    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    		}
    	}

    	// this service REQUIRES an ID - respond with bad request if ID is not supplied
    	if (StringUtils.isNullOrEmpty(id))
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

    	// search the specified image source to find the record we're interested in 
    	SearchHelper<CSpaceImage> dSearchHelper = new SearchHelper<CSpaceImage>();
    	dSearchHelper.addFilter(Derivative.SEARCH.IMAGEID, SEARCHOP.EQUALS, id);
    	List<CSpaceImage> images = imgCtrl.searchImages(sourceScope, dSearchHelper, null);

    	if (images == null || images.size() != 1)
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	// if we have gotten to this point, then we found the unique image and can construct an appropriate response
    	ImageRecord ir = new ImageRecord(images.get(0), true, ts, images);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		RecordSearchController.logSearchResults(request, 1);

		return new ResponseEntity<RecordContainer>(new RecordContainer(ir), headers, HttpStatus.OK);
	}
		 */

	}
}