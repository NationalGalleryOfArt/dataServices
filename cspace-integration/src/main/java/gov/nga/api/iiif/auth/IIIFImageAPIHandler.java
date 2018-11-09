/*
    NGA IIIF Authentication API Implementation: 
    Handler for various URL patterns matching the NGA's IIIF Image API implementation which
    is a bit more complex since we use "/" characters in our image IDs since the IDs refer
    to paths for legacy reasons.  This might change at some point in the future. 

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
package gov.nga.api.iiif.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.Derivative;
import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.integration.cspace.APIUsageException;
import gov.nga.integration.cspace.CSpaceImage;
import gov.nga.integration.cspace.imageproviders.WebImageSearchProvider;
import gov.nga.search.SearchHelper;
import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.ConfigService;

@CrossOrigin(origins="*")
@RestController
@RequestMapping(value={"/${ngaweb.imagingServerIIIFPublicPrefix}/", "/${ngaweb.imagingServerFastCGIPublicPrefix}/"})
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
		CONTENT_TYPE("Content-Type"),
		KEEP_ALIVE("Keep-Alive");
		
		private String label=null;

		private EXCLUDEHEADER(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}
	
	private static Pattern IMGFILENAMEPATTERN = Pattern.compile("(.*\\/)(.*ptif)");
	private static Pattern SAMPLESIZEPATTERN = null;
	
	private static final Map<IIIFAuthParameters, Object> UNAUTHORIZED 	
		= new HashMap<IIIFAuthParameters, Object>() {private static final long serialVersionUID = 1L; {put(IIIFAuthParameters.HTTPSTATUSCODE, HttpStatus.UNAUTHORIZED);}};
	private static final Map<IIIFAuthParameters, Object> NOTFOUND		
		= new HashMap<IIIFAuthParameters, Object>() {private static final long serialVersionUID = 1L; {put(IIIFAuthParameters.HTTPSTATUSCODE, HttpStatus.NOT_FOUND);}};
	
	private static enum IIIFAuthParameters {
		HTTPSTATUSCODE, NGAINTERNAL, NEWSAMPLESIZE, OKTOCACHE, MAXSAMPLESIZE;
	}
	
	@RequestMapping(value="/${ngaweb.imagingServerIIPCGIName}", 
					method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
	public ResponseEntity<InputStreamResource> iipFIFHandler (
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {

		log.trace("entering iipFIFHandler");

		// Step 1: parse the URL and get the image ID
		// Step 2: set the requested sampling size to null because that's what it is for IIP requests (we assume - and don't need to give special treatment to this)
		// Step 3: call common code that fetches the image from the ID/Path and returns the max sample size permissible
		// Step 4: call common code that sets the header as appropriate given the max sample size returned
		// Step 5: call common code that proxies the request, sets / filters caching parameters and returns the response from the server
		
		// retrieve the image path from the first non-zero length FIF parameter value we encounter and call it a day
		String imgPath = null;
		for (String name : request.getParameterMap().keySet() ) {
			if ( name != null && name.toLowerCase().equals("fif") ) {
				for (String v : request.getParameterValues(name) ) {
					if (imgPath == null && v != null && v.length() > 0) {						
						imgPath = v;
						break;
					}
				}
			}
		}
		
		if ( imgPath == null )
			throw new APIUsageException("no image specified in IIP request");

		log.debug(imgPath);
		log.debug(request.getRequestURI());

		String imgVolPath 	= null;
		String imgFilename 	= null;
		Matcher m = IMGFILENAMEPATTERN.matcher(imgPath);
		if (m.find()) {
			imgVolPath	= m.group(1);
			imgFilename	= m.group(2);
		}

		// get image details and if request ends with an unsuccessful httpd status, return that status
		Map<IIIFAuthParameters, Object> imgAuthData = getImageAuthorizationProfile(imgVolPath, imgFilename, null, request, response);
		if ( imgAuthData.containsKey(IIIFAuthParameters.HTTPSTATUSCODE) )
			return ResponseEntity.status((HttpStatus) imgAuthData.get(IIIFAuthParameters.HTTPSTATUSCODE)).body(null);

		// at this point, we should now have the max permitted sampling size so we can just use that in a header and proxy the request to IIP directly
		String serverScheme = cs.getString(Derivative.imagingServerSchemePropertyName);
		String serverURL = cs.getString(Derivative.imagingServerURLPropertyName);
		String publicIIP = cs.getString(IIIFAuthConfigs.iipPublicPrefixPropertyName);
		String privateIIP = cs.getString(IIIFAuthConfigs.iipPrivatePrefixPropertyName);
		String proxyURL = String.format("%s:%s%s?%s", serverScheme, serverURL, request.getRequestURI().replaceAll(publicIIP,  privateIIP), request.getQueryString());
		
		return proxyIIPRequest(
				proxyURL, 
				(Long) imgAuthData.get(IIIFAuthParameters.MAXSAMPLESIZE), 
				(Boolean) imgAuthData.get(IIIFAuthParameters.OKTOCACHE), 
				request, response, serverScheme, serverURL, null
		); 
		// if we have unlimited maxSamplingSize then we just proxy the request normally

	}

	private Long fromSamplingSize(String sampleSize) {
		try {
			return Long.parseLong(sampleSize);
		}
		catch (NumberFormatException nfe) {
			return null;
			// then we interpret this as part of the path to the image rather than a sampling size
		}
	}

	@RequestMapping(value={
			"/{sampleSize}/*/*/{imgFilename:.*}",
			"/{sampleSize}/*/{imgFilename:.*}",
			"/{sampleSize}/{imgFilename:.*}",
			},
			method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST}//,
	)
	public ResponseEntity<InputStreamResource> iiifShortRequestHandler (
			@PathVariable(value="sampleSize") String sampleSize,
			@PathVariable(value="imgFilename") String imgFilename,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {
		return iiifInfoJsonHandler(sampleSize, imgFilename, null, request, response);
	}

	@RequestMapping(value={
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/*/{imgFilename}/{infoJson:info.json}",
			"/{sampleSize}/{imgFilename}/{infoJson:info.json}",
			},
			method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST}//,
	)
	public ResponseEntity<InputStreamResource> iiifInfoJsonHandler (
			@PathVariable(value="sampleSize") String sampleSize,
			@PathVariable(value="imgFilename") String imgFilename,
			@PathVariable(value="infoJson") String infoJson,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {

		log.trace("entering iiifInfoJsonHandler");
		
		String iiifPublicPrefix = cs.getString(IIIFAuthConfigs.iiifPublicPrefixPropertyName);
		String iiifPrivatePrefix = cs.getString(IIIFAuthConfigs.iiifPrivatePrefixPropertyName);

		Long requestedSamplingSize = fromSamplingSize(sampleSize);
		String imgVolPath = null;
		if (requestedSamplingSize != null)
			imgVolPath = request.getRequestURI().replaceFirst(
				Pattern.quote("/"+iiifPublicPrefix+"/"+sampleSize),"").replaceFirst(
				Pattern.quote("/"+imgFilename),""
			);
		else 
			imgVolPath = request.getRequestURI().replaceFirst(
				Pattern.quote("/"+iiifPublicPrefix),"").replaceFirst(
				Pattern.quote("/"+imgFilename),""
			);

		if ( infoJson != null )
			imgVolPath = imgVolPath.replaceFirst(Pattern.quote("/"+infoJson),"");

		log.debug("sampleSize: " + sampleSize);
		log.debug("requestedSamplingSize: " + requestedSamplingSize);
		log.debug("imageVolPath: " + imgVolPath);
		log.debug("imageFilename: " + imgFilename);
		
		Map<IIIFAuthParameters, Object> imgAuthData = getImageAuthorizationProfile(imgVolPath, imgFilename, requestedSamplingSize, request, response);
		if ( imgAuthData.containsKey(IIIFAuthParameters.HTTPSTATUSCODE) )
			return ResponseEntity.status((HttpStatus) imgAuthData.get(IIIFAuthParameters.HTTPSTATUSCODE)).body(null);
		
		Long newSampleSize = (Long) imgAuthData.get(IIIFAuthParameters.NEWSAMPLESIZE);
		if (newSampleSize != null || infoJson == null) {
			// redirect to full size image if newSamplesize is not null and equal to zero
			String redirectURL;
			if (newSampleSize != null && newSampleSize > 0)
				redirectURL = String.format("/%s/%d%s/%s/info.json",iiifPublicPrefix, newSampleSize, imgVolPath, imgFilename);
			else {
				if ( requestedSamplingSize != null )
					redirectURL = String.format("/%s/%d%s/%s/info.json",iiifPublicPrefix, requestedSamplingSize, imgVolPath, imgFilename);
				else
					redirectURL = String.format("/%s%s/%s/info.json",iiifPublicPrefix, imgVolPath, imgFilename);
			}

			// redirect using a temporary redirect if appropriate - use temporary since we don't want browsers to cache the redirect since the
			// response will be different depending on the conditions by which the request was made
			log.debug("redirecting to: " + redirectURL);
			return ResponseEntity.status(HttpStatus.SEE_OTHER).location(new URI(response.encodeRedirectURL(redirectURL))).body(null);	
		}
		
		// otherwise, we proxy to IIP and return the response
		String serverScheme = cs.getString(Derivative.imagingServerSchemePropertyName);
		String serverURL = cs.getString(Derivative.imagingServerURLPropertyName);
		String image_id = String.format("%s/%s", imgVolPath, imgFilename );
		String proxyURL = String.format("%s:%s/%s%s/%s", serverScheme, serverURL, iiifPrivatePrefix, imgVolPath, imgFilename );
		if ( infoJson != null )
			proxyURL += "/" + infoJson;

		return proxyIIPRequest(
				proxyURL, 
				requestedSamplingSize, 
				(Boolean) imgAuthData.get(IIIFAuthParameters.OKTOCACHE), 
				request, response, serverScheme, serverURL+'/'+iiifPublicPrefix, image_id
		);
		
	}
	
	/*
	 * From the Spring Docs
	 * -------
	 * A pattern with a lower count of URI variables and wild cards is considered more specific. For example /hotels/{hotel}/* has 1 URI 
	 * variable and 1 wild card and is considered more specific than  /hotels/{hotel}/** which as 1 URI variable and 2 wild cards.  
	 * 
	 * If two patterns have the same count, the one that is longer is considered more specific. For example /foo/bar* is longer and 
	 * considered more specific than /foo/*.  
	 * 
	 * When two patterns have the same count and length, the pattern with fewer wild cards is considered more specific. 
	 * For example  /hotels/{hotel} is more specific than /hotels/*.
	 * -------
	 *  
	 * Note that some of this is a bit counter intuitive.  That's why the ** wildcard won't work below to match multiple
	 * directories if we ALSO want to use the ** to create a default handler 
	 */
	@RequestMapping(value={
			"/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/*/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}",
			"/{imgFilename}/{region}/{size}/{rotation}/{quality}.{format}"
		}, 
		method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST}
	)
	public ResponseEntity<InputStreamResource> iiifAuthHandler (
			@PathVariable(value="imgFilename", 	required=true)  String imgFilename,
			@PathVariable(value="region", 		required=false) String region,
			@PathVariable(value="size", 		required=false) String size,
			@PathVariable(value="rotation", 	required=false) String rotation,
			@PathVariable(value="quality", 		required=false) String quality,
			@PathVariable(value="format", 		required=false) String format,
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {

		log.trace("entering iiifAuthHandler");

		// PARSE OUT THE SAMPLING SIZE AND THE IMAGE ID / PATH SPECIFIC TO THE FORMAT OF IIIF REQUESTS
		String iiifPublicPrefix = cs.getString(IIIFAuthConfigs.iiifPublicPrefixPropertyName);
		String iiifPrivatePrefix = cs.getString(IIIFAuthConfigs.iiifPrivatePrefixPropertyName);

		// determine whether or not a max sampling size was provided with the request or not
		Long requestedSamplingSize = null;
		String sampleSize = null;
		Matcher m = SAMPLESIZEPATTERN.matcher(request.getRequestURI());
		if (m.find()) {
			sampleSize = m.group(1);
			requestedSamplingSize = fromSamplingSize(sampleSize);
		}
		
		
		/**
		 *  four use cases handled here:
		 *  1 & 2. iiif standard with .jpg region, etc. with and without sample size
		 *  3 & 4. iiif image request for ptif without region, etc. that needs redirect to info.json - with and without sample size 
		 *  
		 *  http://localhost:8100/iiif/public/objects/6/1/61-primary-0-nativeres.ptif/full/512,/0/default.jpg
		 *  http://localhost:8100/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif/full/512,/0/default.jpg
		 *  http://localhost:8100/iiif/public/objects/6/1/61-primary-0-nativeres.ptif		=> redirect to /640  
		 *  http://localhost:8100/iiif/640/public/objects/6/1/61-primary-0-nativeres.ptif	=> redirect to + /info.json
		 */

		// calculate the path to the image so we can find it - we have to extract the relevant parts of the URL in order to do this
		String imgVolPath = null;
		// handle the case where only the image is being requested and defer to the info json handler for this
		if (format != null && format.equals("ptif")) {
			// ok, since ptif isn't an output format, this can only be a request for the pyramidal tiff directly 
			// which we cannot defer to IIP directly without first verifying that the max sample size has been set
			imgFilename = request.getRequestURI().replaceFirst(
				Pattern.quote("/"+iiifPublicPrefix),""
			);
			m = IMGFILENAMEPATTERN.matcher(imgFilename);
			if (m.find()) {
				imgVolPath	= m.group(1);
				imgFilename	= m.group(2);
			}
			if (requestedSamplingSize != null)
				return iiifInfoJsonHandler(requestedSamplingSize.toString(), imgFilename, null, request, response );
			else
				return iiifInfoJsonHandler(null, imgFilename, null, request, response );
		}
		else {
			if (requestedSamplingSize != null)
				imgVolPath = request.getRequestURI().replaceFirst(
					Pattern.quote("/"+iiifPublicPrefix+"/"+sampleSize),"").replaceFirst(
					Pattern.quote("/"+imgFilename+"/"+region+"/"+size+"/"+rotation+"/"+quality+"."+format),""
				);
			else
				imgVolPath = request.getRequestURI().replaceFirst(
					Pattern.quote("/"+iiifPublicPrefix),"").replaceFirst(
					Pattern.quote("/"+imgFilename+"/"+region+"/"+size+"/"+rotation+"/"+quality+"."+format),""
				);
		}

		// LOG SOME IIIF URL SPECIFIC REQUEST PARAMETERS
		log.debug("sampleSize: " + sampleSize );
		log.debug("samplingSize: " + requestedSamplingSize );
		log.debug("imageVolPath: " + imgVolPath);
		log.debug("imageFilename: " + imgFilename);
		log.debug("region: " + region );
		log.debug("size: " + size );
		log.debug("rotation: " + rotation);
		log.debug("quality: " + quality );
		log.debug("format: " + format );

		// AS PER IIIF SPECS, THE if the requested format is not either jpg or png then return a 404
		// however, the validator seems to check for 400, 415, or 503 errors for some reason so we throw a bad request here instead
		IMGFORMAT imgFormat = IMGFORMAT.formatFromExtension(format);
		if ( imgFormat != Derivative.IMGFORMAT.JPEG && imgFormat != Derivative.IMGFORMAT.PNG )
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

		// What we need to do now is figure out whether the image being requested has any special rights constraints that limit the size
		// of the sample that should be used prior to generating the requested image size.  This will simply be a max width and a max height.  
		// We'll convey those to IIP via a custom HTTP HEADER and IIP will read them and make sure that the max tile size it uses considers this; we 
		// only need to do this (for now) if the request was received from outside the firewall - otherwise, we don't set the header and just pass

		// so, what we want to do here is check for the presence of the NGA_INTERNAL header - it will be set by Apache for all internal requests 
		// and unset otherwise so should be fairly reliable - if unset, then we have to validate the request - otherwise we skip the validation part 

		// get image details and if request ends with an unsuccessful httpd status, return that status
		Map<IIIFAuthParameters, Object> imgAuthData = getImageAuthorizationProfile(imgVolPath, imgFilename, requestedSamplingSize, request, response);
		if ( imgAuthData.containsKey(IIIFAuthParameters.HTTPSTATUSCODE) )
			return ResponseEntity.status((HttpStatus) imgAuthData.get(IIIFAuthParameters.HTTPSTATUSCODE)).body(null);

		Long newSampleSize = (Long) imgAuthData.get(IIIFAuthParameters.NEWSAMPLESIZE);

		if (newSampleSize != null) {
			// redirect to full size image if newSamplesize is not null and equal to zero
			String redirectURL;
			if (newSampleSize > 0)
				redirectURL = String.format("/%s/%d%s/%s/%s/%s/%s/%s.%s",iiifPublicPrefix, newSampleSize, imgVolPath, imgFilename, region, size, rotation, quality, format);
			else
				redirectURL = String.format("/%s%s/%s/%s/%s/%s/%s.%s",iiifPublicPrefix, imgVolPath, imgFilename, region, size, rotation, quality, format);

			// redirect using a temporary redirect if appropriate - use temporary since we don't want browsers to cache the redirect since the
			// response will be different depending on the conditions by which the request was made
			log.debug("redirecting to: " + redirectURL);
			return ResponseEntity.status(HttpStatus.SEE_OTHER).location(new URI(response.encodeRedirectURL(redirectURL))).body(null);	
		}

		// otherwise, we proxy to IIP and return the response
		String serverScheme = cs.getString(Derivative.imagingServerSchemePropertyName);
		String serverURL = cs.getString(Derivative.imagingServerURLPropertyName);
		String iiif_request = String.format("%s/%s/%s/%s/%s/%s.%s", imgVolPath, imgFilename, region, size, rotation, quality, format);
		String proxyURL = String.format("%s:%s/%s%s", serverScheme, serverURL, iiifPrivatePrefix, iiif_request);
		// if we have unlimited maxSamplingSize then we just proxy the request normally

		return proxyIIPRequest(proxyURL, requestedSamplingSize, (Boolean) imgAuthData.get(IIIFAuthParameters.OKTOCACHE), request, response, serverScheme, serverURL, iiif_request );
	}
	
	// in the future, e.g. eDAM context, we can easily create a method that accepts the image ID
	public Map<IIIFAuthParameters, Object> getImageAuthorizationProfile(String imgVolPath, String imgFilename, Long requestedSamplingSize, HttpServletRequest request, HttpServletResponse response) {

		Map<IIIFAuthParameters, Object> returnMap = CollectionUtils.newHashMap();
		
		SearchHelper<CSpaceImage> dSearchHelper = new SearchHelper<CSpaceImage>();
		if ( !imgVolPath.startsWith("/") )
			imgVolPath = "/" + imgVolPath;
		if ( !imgVolPath.endsWith("/") )
			imgVolPath += "/";
		
		// TODO -- for now, this is an afterthought hack since conservation space thumbnails cannot be displayed otherwise, but 
		// in the future, however, this should be handled by permissions in the eDAM instead of having to read private art object data
		// to ascertain the rights of an image
		if ( imgVolPath.startsWith("/private") ) {
			// returnMap.put(IIIFAuthParameters.NGAINTERNAL, 		ngainternal);
			returnMap.put(IIIFAuthParameters.OKTOCACHE, 		true);
			returnMap.put(IIIFAuthParameters.MAXSAMPLESIZE,		null);
			return returnMap;
		}
		
		dSearchHelper.addFilter(Derivative.SEARCH.IMAGEVOLUMEPATH, SEARCHOP.EQUALS, imgVolPath);
		dSearchHelper.addFilter(Derivative.SEARCH.IMAGEFILENAME, SEARCHOP.EQUALS, imgFilename);

		List<CSpaceImage> images = null;
		try {
			images = webImageSearchProvider.searchImages(dSearchHelper, null);
		}
		catch (ExecutionException | InterruptedException ie) {
			return NOTFOUND;
		}

		// we should only ever have one image with the same volumepath and filename given the way we handle object images right now
		log.debug("found: " + images.size() + " images");

		Long maxPermittedSamplingSize = null;

		// read header set by Apache to determine whether this is an internal or external request
		boolean ngainternal = request.getHeader("NGA_EXTERNAL") == null && request.getHeader("NGA_INTERNAL") != null;
		log.debug("NGA INTERNAL: " + ngainternal);

		CSpaceImage d = null;

		// if we don't have any images, then it is safe to just defer to the image server to handle them like it did before this layer was written
		// the only images that would be published would have rights cleared ahead of time anyway - that said, in the future ALL of the image rights 
		// will need to come from the eDAM
		if (images != null && images.size() > 0)	
			d = images.get(0);

		// if the requested image isn't a zoom image for some reason, then return a 404 error since we can't return a IIIF image from a non-zoomable file
		if ( d != null && !d.isZoom() )
			return NOTFOUND;
	    
		// acquire max sampling size from APIs
		if ( d != null )
			maxPermittedSamplingSize = d.getMaxSamplingSizeInPixels(!ngainternal);
		// scenario image is 200 but max sampling is 700 - but request was for default so we just don't enforce the header that's all - no redirect

		// if we are not permitted to show this image, then return an unauthorized message
		if ( maxPermittedSamplingSize != null && maxPermittedSamplingSize <= 0 )
			return UNAUTHORIZED;

		// if the requested sampling size is greater than the max sampling size or there isn't a requested max size and the max size is greater than or equal to the actual width 
		// of the image then just redirect to the default and recalculate <-- endless redirect
		if ( requestedSamplingSize != null && ( ( maxPermittedSamplingSize != null && requestedSamplingSize > maxPermittedSamplingSize ) || requestedSamplingSize <= 0 || (d != null && (requestedSamplingSize >= d.getWidth() || requestedSamplingSize >= d.getHeight()) ) ) ) { 
			requestedSamplingSize = null;
			maxPermittedSamplingSize = null;
			returnMap.put(IIIFAuthParameters.NEWSAMPLESIZE, new Long(0L)); // tells client to redirect to the full size image
		}
		// no point sending the max sampling size header if the max is greater than the longest side of the image anyway
		if ( maxPermittedSamplingSize != null && ( d != null && (maxPermittedSamplingSize >= d.getWidth() || maxPermittedSamplingSize >= d.getHeight()) ) )
			maxPermittedSamplingSize = null;

		// if a sample size is specified in the URL already or there's a max sample size to enforce
		// then we might have to redirect to a different URL using a new sample size
		if ( requestedSamplingSize != null || maxPermittedSamplingSize != null ) {
			Long newSampleSize = null;

			// if max sample size is in play but no sample size was specified OR the sample size user requested is larger than the max permissible
			if ( maxPermittedSamplingSize != null && ( requestedSamplingSize == null || requestedSamplingSize > maxPermittedSamplingSize ) )
				newSampleSize = maxPermittedSamplingSize;

			if ( d != null) {
				// if the sample size requested is larger than the width or height of the zoom image, then constrain it to the larger of the two
				if ( requestedSamplingSize != null && ( requestedSamplingSize > d.getWidth() || requestedSamplingSize > d.getHeight() ) )
					newSampleSize = d.getWidth() > d.getWidth() ? d.getWidth() : d.getHeight();

					// and finally, if the new sample size is >= the width or height of the image, then no point in specifying maxsampling at all
					if ( newSampleSize != null && ( newSampleSize >= d.getWidth() || newSampleSize >= d.getHeight() ) )
						newSampleSize = null;
			}

			returnMap.put(IIIFAuthParameters.NEWSAMPLESIZE, newSampleSize);

		}

		// by default allow caching of everything
		boolean oktocache = true;
		if ( d != null) {
			// but don't cache requests for default images (samplingSize == null) with public sampling restrictions that are smaller than the image's longest side
			Long maxPublicPix = d.getMaxSamplingSizeInPixels(true);
			if ( requestedSamplingSize == null && maxPublicPix != null && maxPublicPix < d.getLongestSideInPixels() )
				oktocache = false;
		}

		//returnMap.put(IIIFAuthParameters.NGAINTERNAL, 		ngainternal);
		returnMap.put(IIIFAuthParameters.OKTOCACHE, 		oktocache);
		returnMap.put(IIIFAuthParameters.MAXSAMPLESIZE,		maxPermittedSamplingSize);
		
		return returnMap;

	}
	
	public ResponseEntity<InputStreamResource> proxyIIPRequest (
			String proxyURL,
			Long samplingSizeToEnforce,
			boolean permitCaching,
			HttpServletRequest request,
			HttpServletResponse response,
			String iiif_scheme,
			String iiif_prefix,
			String image_id
			) throws Exception {

		
		HttpURLConnection urlConnection = null;
		try {

			URI imageURI = new URI(proxyURL);

			log.debug("proxying to: " + imageURI.toString());

			urlConnection = (HttpURLConnection) imageURI.toURL().openConnection();
			urlConnection.setInstanceFollowRedirects(false); // don't follow IIP redirects automatically behind the scenes - we want to return those to browser
			// add a special HTTP header here to instruct IIP to only take up to a certain maximum tile size when resampling if we have such a restriction
			urlConnection.setUseCaches(false);

			String iiif_image_id = null;
			if (image_id != null)
				iiif_image_id = String.format("%s:%s%s", iiif_scheme, iiif_prefix, image_id);

			// this might seem silly to set three headers for the same thing, and admittedly it is
			// but in Apache 2.4, it seems headers with underscores set by the client are converted to
			// headers with dashes - in light of this, I'm going to try to transition away from using any
			// word delimiters. Putting all three at once for a while ensures that this program continues
			// to be backwards compatible with the original IIP implementation that expects underscores
			if ( samplingSizeToEnforce != null ) {
				urlConnection.setRequestProperty("MAXSAMPLESIZE",   samplingSizeToEnforce.toString());
				urlConnection.setRequestProperty("MAX_SAMPLE_SIZE", samplingSizeToEnforce.toString());
				urlConnection.setRequestProperty("MAX-SAMPLE-SIZE", samplingSizeToEnforce.toString());
				if (image_id != null)
					iiif_image_id = String.format("%s:%s/%s%s", iiif_scheme, iiif_prefix, samplingSizeToEnforce.toString(), image_id);
			}
			// override the IIIF Image ID that IIP uses to ensure the size constraint is reported in the ID as this is very important to
			// establish for the purpose of annotation, etc.  Also, a IIIF Image request to a region INSIDE a size-constrained image will NOT
			// match the region inside a non-size constrained image, so it makes sense that these two image renditions would have different IDs
			if (iiif_image_id != null) {
				urlConnection.setRequestProperty("XIIIFID",   iiif_image_id );
				urlConnection.setRequestProperty("X_IIIF_ID", iiif_image_id );
				urlConnection.setRequestProperty("X-IIIF-ID", iiif_image_id );
			}
			
			if ( request.getHeader("NGA_EXTERNAL") != null) {
				urlConnection.setRequestProperty("NGAEXTERNAL",  "true");
				urlConnection.setRequestProperty("NGA_EXTERNAL",  "true");
				urlConnection.setRequestProperty("NGA-EXTERNAL",  "true");
			}
			
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
							if ( eh != EXCLUDEHEADER.CACHE_CONTROL || !permitCaching )
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

			if ( !permitCaching )
				response.setHeader(EXCLUDEHEADER.CACHE_CONTROL.getLabel(), "no-cache");
			
			return ResponseEntity.status(urlConnection.getResponseCode())
					.contentType(MediaType.parseMediaType(urlConnection.getContentType() != null ? urlConnection.getContentType() : MediaType.TEXT_PLAIN.toString()))
					.body(new InputStreamResource(urlConnection.getInputStream()));
		}
		catch (URISyntaxException se) {
				throw new APIUsageException(se.getMessage());
		}
		catch (IOException ie) {
			if ( urlConnection.getErrorStream() != null)
				return ResponseEntity.status(urlConnection.getResponseCode())
						.contentType(MediaType.parseMediaType(urlConnection.getContentType() != null ? urlConnection.getContentType() : MediaType.TEXT_PLAIN.toString()))
						.body(new InputStreamResource(urlConnection.getErrorStream()));
			else 
				return ResponseEntity.status(urlConnection.getResponseCode())
						.contentType(MediaType.parseMediaType(urlConnection.getContentType() != null ? urlConnection.getContentType() : MediaType.TEXT_PLAIN.toString()))
						.body(null);
		}
	}
	
	@PostConstruct
	public void postConstruct() throws Exception {
		if (SAMPLESIZEPATTERN == null)
			SAMPLESIZEPATTERN = Pattern.compile("/"+cs.getString(IIIFAuthConfigs.iiifPublicPrefixPropertyName)+"\\/(\\d*)\\/");
	}
	
/*	@RequestMapping("**")
	public ResponseEntity<String> iiifUnMatchedRequestHandler (
			HttpServletRequest request,
			HttpServletResponse response
			) throws Exception {
		log.trace("entering iiifUnmatchedRequestHandler");
		throw new APIUsageException("Bogus request");
	}
	*/
	
	// EXCEPTION HANDLER - WE DON'T CARE IF THE CLIENT ABORTS SO JUST SWALLOW THIS EXCEPTION AND DON'T LOG ANYTHING
	@ExceptionHandler(ClientAbortException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public void handleClientAbortException(ClientAbortException e, HttpServletRequest req) {
		log.trace("entering handleClientAbortException");
		// nothing to return to client since they have already aborted the connection
	}

	// EXCEPTION HANDLER - SPECIFIC TO THIS CONTROLLER - CANNOT RESPOND WITH AN ACTUAL RESPONSE HERE OR WE GET A MEDIA TYPE ERROR
	@ExceptionHandler(APIUsageException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleAPIUsageException(APIUsageException e, HttpServletRequest req) {
		log.trace("entering handleAPIUsageException");
		return "Bad Request.  Consult the <a href=\"http://iiif.io/image/\">IIIF Image Specifications</a> or the <a href=\"http://iipimage.sourceforge.net/documentation/protocol/\">IIP Protocol</a> and try re-formulating the request.";
	}
	
	// DEFAULT EXCEPTION HANDLER - SPECIFIC TO THIS CONTROLLER - CANNOT RESPOND WITH AN ACTUAL RESPONSE HERE OR WE GET A MEDIA TYPE ERROR
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String handleAnyOtherException(Exception e, HttpServletRequest req) {
		log.trace("entering handleAnyOtherException", e);
		log.debug("An unhandled exception encountered: ", e);
		return "An error occurred trying to handle your request.  Try your request again later or try reformulating it.";
	}
 	
	
	
}
