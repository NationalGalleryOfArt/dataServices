/*
    NGA Art Data API: Derivative is a class representing an image
    derivative such as a resize of a larger image or a zoom image
    created from a master image, etc.  This class and the other image
    classes are badly in need of a major refactoring in light of the
    fact that IIIF images are now available for all art objects and
    the fact that ALL of our web images from the new NetX DAM will be
    made available over IIIF.   

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
package gov.nga.entities.art; 

import gov.nga.search.Faceted;
import gov.nga.search.SearchFilter;
import gov.nga.search.Searchable;
import gov.nga.search.SortHelper;
import gov.nga.search.SortOrder;
import gov.nga.search.Sortable;
import gov.nga.search.Sorter;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.DateUtils;
import gov.nga.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Derivative extends SupplementingEntityImpl implements Searchable, Sortable, Faceted  {

	private static final Logger log = LoggerFactory.getLogger(Derivative.class);

	// maximum pixel extent to be considered a fair use image
	protected static final long FAIRUSEMAXEXTENT = 640;

	// property name containing the base URL of the imaging server 
	public static final String imagingServerURLPropertyName = "imagingServerURL";
	public static final String imagingServerSchemePropertyName = "imagingServerScheme";


	public static enum IMAGECLASS {
		ARTOBJECTIMAGE, RESEARCHIMAGE, CONSERVATIONIMAGE
	}

	// derivative search fields
	public static enum SEARCH {
		CATALOGUED,
		IMAGEID,
		IMAGEVOLUMEPATH,
		IMAGEFILENAME
	}

	public static enum SORT {
		IMAGEID_ASC,
		IMAGEID_DESC,
		CATALOGUED_ASC,
		CATALOGUED_DESC
	}

	// it's important to use a reasonably fast default sort order
	// because this will be called every time a list of objects is
	// generated by a search
	private static final SortOrder defaultSortOrder = new SortOrder(SORT.IMAGEID_ASC);

	// it's important to use a fast default sort order 
	private static final SortOrder naturalSortOrder = defaultSortOrder;

	public SortOrder getDefaultSortOrder() {
		return defaultSortOrder;
	}

	public SortOrder getNaturalSortOrder() {
		return naturalSortOrder;
	}

	public static enum ImgSearchOpts {
		FALLBACKTOLARGESTFIT, 
		PREFERCROP,
		NOOP
	}

	public static enum IMGVIEWTYPE {
		PRIMARY("primary"),
		CROPPED("crop"),
		ALTERNATE("alternate"),
		COMPFIG("compfig"),
		TECHNICAL("technical"),
		INSCRIPTION("inscription");

		private String label=null;

		private IMGVIEWTYPE(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public static IMGVIEWTYPE toViewType(String vt) {
			if (vt == null)
				return null;
			for (IMGVIEWTYPE i : IMGVIEWTYPE.values()) {
				if (i.getLabel().equalsIgnoreCase(vt))
					return i;
			}
			return null;
		}

		public static Collection<IMGVIEWTYPE> allViewTypesExcept(IMGVIEWTYPE... exceptFor) {
			List<IMGVIEWTYPE> allValues = CollectionUtils.newArrayList(Arrays.asList(IMGVIEWTYPE.values()));
			for (IMGVIEWTYPE vt : exceptFor)
				allValues.remove(vt);
			return allValues;
		}
	}

	// not all of these are actual image formats, but Portfolio accepts them so for now we'll leave here
	// we will need to eventually base Derivative off of a more generic media class which would possibly contain some of these
	// or the mimetype can be detected dynamically after we know more about the actual bits of the file
	// TODO: CONVERT this to use Spring's MediaType class
	public static enum IMGFORMAT {
		JPEG(	new String[]{".jpg",".jpeg"},	"JPEG",	"image/jpeg"),
		PNG(	new String[]{".png"},			"PNG",	"image/png"),
		TIFF(	new String[]{".tif",".tiff"}, 	"TIFF",	"image/tiff"),
		PTIF(	new String[]{".ptif"},			"PTIF",	"image/tiff"),
		BMP(	new String[]{".bmp"},			"BMP",	"image/bmp"),
		CR2(	new String[]{".cr2"}, 			"CR2", 	"image/x-canon-cr2"),
		NEF(	new String[]{".nef"}, 			"NEF", 	"image/x-nikon-nef"),
		DNG(	new String[]{".dng"}, 			"CR2", 	"image/x-adobe-dng"),
		AVI(	new String[]{".avi"}, 			"AVI", 	"video/avi"),
		MOV(	new String[]{".mov"}, 			"MOV", 	"video/quicktime"),
		DOCX(	new String[]{".docx"}, 			"DOCX",	"application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
		PPTX(	new String[]{".pptx"}, 			"PPTX",	"application/vnd.openxmlformats-officedocument.presentationml.presentation"),
		PDF(	new String[]{".pdf"}, 			"PDF", 	"application/pdf"),
		PSD(	new String[]{".psd"},			"PSD",	"application/photoshop"),
		XMP(	new String[]{".xmp"},			"XMP",	"application/rdf+xml"),
		BIN(	new String[]{".bin"},			"BIN",	"application/octet-stream");

		private String label;
		private String preferredExtension;
		private String[] extensions;
		private String mimetype;

		private IMGFORMAT(String[] exts, String label, String mimetype) {
			this.label = label;
			this.extensions = exts;
			this.preferredExtension = exts[0];
			this.mimetype = mimetype;
		}

		public String getLabel() {
			return label;
		}

		public String getExtension() {
			return preferredExtension;
		}

		public String[] getExtensions() {
			return extensions;
		}

		public String getMimetype() {
			return mimetype;
		}

		public static IMGFORMAT formatFromExtension(String ext) {
			ext = ext.toLowerCase();
			for (IMGFORMAT f : IMGFORMAT.values() ) {
				for (String e : f.getExtensions() ) {
					if (e.equals(ext) || e.equals("." + ext))
						return f;
				}
			}
			return IMGFORMAT.BIN;	// use a generic binary format as the default when one cannot be found
		}

	}

	public Derivative(ArtDataManagerService manager) {
		super(manager,null);
	}

	protected abstract String getAllImagesQuery();

	public abstract Derivative factory(ResultSet rs) throws SQLException;

	public static Comparator<Derivative> sortByPixelSizeAsc = new Comparator<Derivative>() {
		public int compare(Derivative a, Derivative b) {
			return Long.compare(a.pixelCount(), b.pixelCount());
		}
	};

	// used for executing searches
	public Boolean matchesFilter(SearchFilter filter) {
		switch ( (SEARCH) filter.getField()) {
		case CATALOGUED:
			return getCatalogued() == null ? false : filter.filterMatch(getCatalogued().toString());
		case IMAGEID:
			return getImageID() == null ? false : filter.filterMatch(getImageID().toString());
		case IMAGEVOLUMEPATH:
			return getImgVolumePath() == null ? false : filter.filterMatch(getImgVolumePath());
		case IMAGEFILENAME:
			return getFilename() == null ? false : filter.filterMatch(getFilename());
		}
		return false;
	}

	// we don't currently have any cases where we are comparing two derivatives with a third
	public Long matchesAspect(Object ao, Object order) {
		return null;
	}

	// used for sorting derivatives or for deferring sorting to an ArtObject if one is associated with the derivative and 
	// ArtObject sort criteria happen to be in use
	public int aspectScore(Object od, Object order, String matchString) {
		Derivative d = (Derivative) od;
		if (d == null || order == null)
			return Sorter.NULL;

		if ( order instanceof SORT ) {
			switch ((SORT) order) {
			case CATALOGUED_ASC:
				return SortHelper.compareObjects(getCatalogued(), d.getCatalogued());
			case CATALOGUED_DESC:
				return SortHelper.compareObjects(d.getCatalogued(), getCatalogued());
			case IMAGEID_ASC:
				return SortHelper.compareObjects(getImageID(), d.getImageID());
			case IMAGEID_DESC:
				return SortHelper.compareObjects(d.getImageID(), getImageID());
			}
		}
		else if ( order instanceof ArtObject.SORT) {
			ArtObject o1 = getArtObject();
			ArtObject o2 = d.getArtObject();
			// if objects are null or the same object
			if (o1 == o2)
				return Sorter.NULL;
			if (o1 == null)
				return 1;
			if (o2 == null)
				return -1;
			return o1.aspectScore(o2, order, matchString);
		}

		return Sorter.NULL;
	}

	public long pixelCount() {
		return Math.abs(getWidth() * getHeight());
	}

	public long pixelDiff(long pixels) {
		return Math.abs(pixels - pixelCount());
	}

	public Boolean sameDimensions(Derivative i) {
		return getWidth() == i.getWidth() && getHeight() == i.getHeight();
	}

	public Boolean sameTargetDimensions(Derivative i) {
		return getTargetWidth() == i.getTargetWidth() && getTargetHeight() == i.getTargetHeight();
	}

	public String getSourceImageName() {
		return getArtObjectID() + "_" + getViewType() + "_" + getSequence() ;
	} 


	// if we have a cropped image that fits the exact target box
	// then we return that instead
	private static <I extends Derivative> I processCropPref(List<I> images, I i, long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {

		// we only process the cropped image preference when we're already looking
		// for primary images with sequence zero
		if (vt == null || !vt.equals(IMGVIEWTYPE.PRIMARY) || seq == null || !seq.equals("0") )
			return i;

		// if user prefers crop
		if (i != null && i.isCropped())
			return i;

		boolean wantcrop = false;
		for (ImgSearchOpts opt : opts) {
			if ( opt == ImgSearchOpts.PREFERCROP ) {
				wantcrop = true;
				break;
			}
		}

		if (wantcrop && images != null) {

			for (I ic : images) {
				// if this image is cropped and its height and width
				// exactly match the target width and height that we're looking for
				// then return it rather than the non-cropped version
				if (ic.isCropped() && ic.getWidth() == targetWidth && ic.getHeight() == targetHeight ) {
					i = ic;
					break;
				}
			}
		}
		return i;
	}

	private static boolean filterMatch(Derivative i, IMGVIEWTYPE vt, String seq) {
		return (vt == null || i.getViewType().equals(vt))  && (seq == null || i.getSequence().equals(seq)); 
	}

	protected static <I extends Derivative> List<String> getAvailableImageSequences(List<I> images, IMGVIEWTYPE vt) {
		Map<String,Object> map = CollectionUtils.newHashMap();
		if (images != null) {
			for (I i : images) {
				if ( filterMatch(i,vt,null) )
					map.put(i.getSequence(),null);
			}
		}
		return CollectionUtils.newArrayList(map.keySet());
	}

	protected static List<IMGVIEWTYPE> getAvailableImageViewTypes(List<Derivative> images) {
		Map<IMGVIEWTYPE,Object> map = CollectionUtils.newHashMap();
		if (images != null) {
			for (Derivative i : images) {
				map.put(i.getViewType(),null);
			}
		}
		return CollectionUtils.newArrayList(map.keySet());
	}

	public static <I extends Derivative> I getImageClosestPixels(List<I> images, long numPixels, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		I ip = null;
		if (images != null) {
			for (I i : images) {
				if ( filterMatch(i,vt,seq) ) {
					if (ip == null)
						ip = i;
					else if (i.pixelDiff(numPixels) < ip.pixelDiff(numPixels))
						ip = i;
				}
			}
		}
		return ip;
	}

	public static <I extends Derivative> I getLargestZoomImage(List<I> images, IMGVIEWTYPE vt, String seq) {
		I ip = null;
		if (images != null) {
			for (I i : images) {
				if ( filterMatch(i,vt,seq) ) {
					if ( i.isZoom() && ( ip == null || i.pixelCount() > ip.pixelCount() ) )
						ip = i;
				}
			}
		}
		return ip;
	}

	public static <I extends Derivative> I getImageExactTargetBoxMatch(List<I> images, long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		if (images != null) {
			for (I i : images) {
				// skip over any cropped images or native resolution images
				if ( filterMatch(i,vt,seq)) {
					if (i.getTargetWidth() == null || i.getTargetHeight() == null)
						continue;
					if (    i.getTargetWidth() == targetWidth && 
							i.getTargetHeight() == targetHeight)
						// if we have a cropped image of the exact size as the target widths and heights
						// and a preference was expressed for cropped images when available, then
						// return the crop instead
						return processCropPref(images, i, targetWidth, targetHeight, vt, seq, opts);
				}
			}
		}
		return null;
	}

	public static <I extends Derivative> I getLargestImage(List<I> images, IMGVIEWTYPE vt, String seq, IMGFORMAT... preferredFormatOrder) {
		I ip = null;

		IMGFORMAT[] defaultFormatOrder = {IMGFORMAT.PTIF, IMGFORMAT.JPEG, IMGFORMAT.PNG}; 
		if (preferredFormatOrder.length == 0)
			preferredFormatOrder = defaultFormatOrder;

		if (images != null) {
			for (I i : images) {
				if ( filterMatch(i,vt,seq)) {
					// skip over any cropped images or images that are too large
					if (ip == null || i.pixelCount() > ip.pixelCount())
						ip = i;
					else if (i.pixelCount() == ip.pixelCount()) {
						// we have two images equal in size, and we need to select one of them based on a preferred format order                    	
						for (IMGFORMAT f : preferredFormatOrder) {
							if (f.equals(ip.getFormat()))
								break;
							if (f.equals(i.getFormat())) {
								ip = i;
								break;
							}
						}
					}
				}
			}
		}
		// if we have a cropped image of the exact size target box
		// as the best non-cropped image we found, then return it instead
		// if a preference was expressed for cropped images

		return ip;
	}

	public static <I extends Derivative> I getLargestImageFittingTarget(List<I> images, long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {
		I ip = null;
		if (images != null) {
			for (I i : images) {
				if ( filterMatch(i,vt,seq)) {
					// skip over any cropped images or images that are too large
					if (i.getWidth() > targetWidth || i.getHeight() > targetHeight)
						continue;
					if (ip == null || i.pixelCount() > ip.pixelCount())
						ip = i;
				}
			}
		}
		// if we have a cropped image of the exact size target box
		// as the best non-cropped image we found, then return it instead
		// if a preference was expressed for cropped images

		return processCropPref(images, ip, targetWidth, targetHeight, vt, seq, opts);
	}

	public static <I extends Derivative> List<I> getLargestImagesFittingTarget(List<I> images, long targetWidth, long targetHeight, IMGVIEWTYPE vt, ImgSearchOpts... opts) {
		List<I> largestMatches = CollectionUtils.newArrayList();
		if (images != null) {
			List<I> imagesOfViewType = getImagesByViewType(images, vt);
			List<String> sequences = getAvailableImageSequences(imagesOfViewType, vt);
			for (String seq : sequences) {
				I ip = null;
				for (I i : imagesOfViewType) {
					// if the image is of the desired view type and matches our sequence loop
					// then find the best fit for this view type / sequence  
					if ( filterMatch(i,vt,seq)) {
						// skip over any cropped images, zoom images, or images that are too large
						if (i.getWidth() > targetWidth || i.getHeight() > targetHeight || i.isZoom())
							continue;
						if (ip == null || i.pixelCount() > ip.pixelCount())
							ip = i;
					}
				}
				// if we have a cropped image of the exact size target box
				// as the best non-cropped image we found, then return it instead
				// if a preference was expressed for cropped images
				ip = processCropPref(images, ip, targetWidth, targetHeight, vt, seq, opts);
				if (ip != null)
					largestMatches.add(ip);
			}
		}
		return largestMatches;
	}

	public static <I extends Derivative> List<I> getImagesByViewType(List<I> images, IMGVIEWTYPE vt) {
		List<I> l = CollectionUtils.newArrayList();
		for (I i : images) {
			if (i.getViewType() == vt)
				l.add(i);
		}
		return l;
	}

	protected static URI createURI(String scheme, String ssp) {
		try {
			return new URI(scheme,ssp,null);
		}
		catch (URISyntaxException ue) {
			log.error("URISyntaxException when creating source image URL: ", ue);
			return null;
		}
	}

	protected static URI createURI(String ssp) {
		return createURI(null, ssp);
	}

	public URI getSourceImageURI() {
		return getSourceImageURI(null);
	}

	public URI getSourceImageURI(String forceScheme) {
		return 	createURI(
				forceScheme, 
				getManager().getConfig().getString(imagingServerURLPropertyName) + this.getImgVolumePath() + this.getFilename() 
				);
	}

	public URI getRelativeSourceImageURI() {
		return 	createURI(
				null, 											// no scheme 
				this.getImgVolumePath() + this.getFilename()	// and no host - only relative path
				);
	}
	
	public URL getIIIFBaseURL() throws MalformedURLException {
		URI u = getProtocolRelativeiiifURL();
		return u == null ? null : new URL("https:" + u.toString());
	}

	public URI getProtocolRelativeiiifURL(String region, String size, String rotation, String quality) {
		// iiif URLs are only valid for PTIF files that are on a IIIF enabled server
		if (format != IMGFORMAT.PTIF)
			return null;

		if (region != null || size != null || rotation != null || quality != null) { 
			if (StringUtils.isNullOrEmpty(region))
				region = "full";
			if (StringUtils.isNullOrEmpty(size))
				size = "full";
			if (StringUtils.isNullOrEmpty(rotation))
				rotation = "0";
			if (StringUtils.isNullOrEmpty(quality))
				quality = "default";
		}

		try {
			String iiifPath = 
					getManager().getConfig().getString(imagingServerURLPropertyName) 
					+ "/iiif" + getRelativeSourceImageURI().toString();
			if (region != null && size != null && rotation != null && quality != null)
				iiifPath += "/" + region + "/" + size + "/" + rotation + "/" + quality + ".jpg";
			return new URI(iiifPath);
		}
		catch (URISyntaxException ue) {
			log.error("URISyntaxException when creating iiif source image URL: ", ue);
			return null;
		}
	}
	
	public URI getProtocolRelativeiiifURL() {
		return getProtocolRelativeiiifURL(null,null,null,null);
	}

	public abstract IMAGECLASS getImageClass();

	private String imageID = null;
	public void setImageID(String imageID) {
		this.imageID = imageID;
	}
	public String getImageID() {
		return imageID;
	}

	private String imgVolumePath = null;
	public void setImgVolumePath(String imgVolumePath) {
		this.imgVolumePath=imgVolumePath;
	}
	public String getImgVolumePath() {
		return imgVolumePath;
	}

	private String filename = null;
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setFormat(IMGFORMAT format) {
		this.format = format;
	}

	protected void setFormatFromExtension(String ext) {
		this.format = IMGFORMAT.formatFromExtension(ext);
		if (this.format == null)
			log.warn("no image format detected for extension " + ext);
	}

	public void setWidth(Long width) {
		this.width = width;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public void setTargetWidth(Long targetWidth) {
		this.targetWidth = targetWidth;
	}

	public void setTargetHeight(Long targetHeight) {
		this.targetHeight = targetHeight;
	}

	public void setViewType(IMGVIEWTYPE viewType) {
		this.viewType = viewType;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getFilename() {
		return filename;
	}

	private IMGFORMAT format = null;
	public IMGFORMAT getFormat() {
		return format;
	}

	public Boolean isZoom() {
		return getFormat().equals(IMGFORMAT.PTIF);
	}

	public Boolean isCropped() {
		return getViewType() != null ? getViewType().equals(IMGVIEWTYPE.CROPPED) : false;
	}

	private Long width = null;
	public Long getWidth() {
		return width;
	}

	private Long height = null;
	public Long getHeight() {
		return height;
	}

	private Long targetWidth = null;
	public Long getTargetWidth() {
		return targetWidth;
	}

	private Long targetHeight = null;
	public Long getTargetHeight() {
		return targetHeight;
	}

	private IMGVIEWTYPE viewType = null;
	public IMGVIEWTYPE getViewType() {
		return viewType;
	}

	private String sequence = null;
	public String getSequence() {
		return sequence;
	}

	private String catalogued = null;
	public String getCatalogued() {
		return catalogued;
	}

	public void setCatalogued(String catalogued) {
		this.catalogued = catalogued;
	}

	protected void setCatalogued(Date catalogued) {
		if (catalogued != null) {
			setCatalogued(DateUtils.formatDate(DateUtils.DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT, catalogued));
		}
	}

	private String source = null;
	public void setSource(String source) {
		this.source = source;
	}
	public String getSource() {
		return this.source;
	}

	public String getTitle() {
		if (getArtObjectID() == null)
			return null;
		// return the title of the NGA art object associated with this image
		ArtObject ao = null;
		try {
			ao = getManager().fetchByObjectID(getArtObjectID());
		} catch (Exception exception) { // ?????? what exceptions and why catch them?
			log.error("Can not fetch By Object ID");
		}
		if (ao != null)
			return "_" + ao.getTitle() + "_";

		return null;
	}

	private String testingMessage;
	public String getTestingMessage() {
		return testingMessage;
	}

	public void setTestingMessage(String testingMessage) {
		this.testingMessage = testingMessage;
	}
	
	public Long getLongestSideInPixels() {
		return this.width > this.height ? this.width : this.height;
	}

	// return the maximum pixel sampling size we can use for IIIF images (or for returning pre-computed derivatives from a larger image)
	// for public audience we are only permitted to use a sample that is at most a bounding box of this dimension.  For example, if we have 
	// a source image that is 30,000 pixels by 20,000 pixels but we aren't allowed to show zoom imagery for the object, then by fair use 
	// rules, we can only sample based on fair use sizes (currently 440 pixels in long dimension) - by extension, we probably shouldn't upscale but that's actually ok in cases where a client requests
	// an upscaled sample explicitly
	public Long getMaxSamplingSizeInPixels(boolean publicAudience) {

		// by default we can't show the image at all
		Long maxPixels = 0L;
		
		if ( !publicAudience )
			return getLongestSideInPixels();

		// if this image is of an art object, then we need to inspect the permissions of the underlying art object 
		ArtObject o = getArtObject(); 
		if ( o != null && o.isPublic() ) {
			// no restriction on sample size if art object is public and zoom imagery is permitted for the object
			if ( o.isZoomImageryPermitted() )
				maxPixels = null;
			else if ( ! o.isThumbnailProhibited() )
				maxPixels = FAIRUSEMAXEXTENT < getLongestSideInPixels() ? FAIRUSEMAXEXTENT : getLongestSideInPixels();
			// otherwise we shouldn't be allowing this image to be shown at all
			else
				return 0L;
		}
		// otherwise, if the image isn't of an art object, we currently have no way to convey any limitations
		// this will change in the future when the eDAM is released and we have metadata that captures that
		// so, in the future, we'll have to check the rights on the file in the eDAM for non-object - otherwise 
		// but for now we assume that all images are public
		else 
			maxPixels = null;
		
		return maxPixels;
	}
	
}

