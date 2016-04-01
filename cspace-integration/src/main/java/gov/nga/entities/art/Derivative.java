
package gov.nga.entities.art; 

import gov.nga.utils.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Derivative extends ArtEntityImpl {

    private static final Logger log = LoggerFactory.getLogger(Derivative.class);

    // maximum pixel extent to be considered a fair use image
    protected static final long FAIRUSEMAXEXTENT = 250;
    
    // property name containing the base URL of the imaging server 
    protected static final String imagingServerURLPropertyName = "imagingServerURL";

    public static enum IMAGECLASS {
        ARTOBJECTIMAGE, RESEARCHIMAGE
    };

    public static enum ImgSearchOpts {
        FALLBACKTOLARGESTFIT, 
        PREFERCROP,
        NOOP
    };

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
    }

    public static enum IMGFORMAT {
        PTIF(".ptif","PTIF"),
        JPEG(".jpg","JPEG");

        private String label=null;
        private String ext=null;

        private IMGFORMAT(String ext, String label) {
            this.label = label;
            this.ext = ext;
        }

        public String getLabel() {
            return label;
        }

        public String getExtension() {
            return ext;
        }

    }
    
    public Derivative(ArtDataManagerService manager) {
        super(manager);
    }
    
    protected abstract String getAllImagesQuery();

    public abstract Derivative factory(ResultSet rs) throws SQLException;

    public static Comparator<Derivative> sortByPixelSizeAsc = new Comparator<Derivative>() {
        public int compare(Derivative a, Derivative b) {
            return new Long(a.pixelCount()).compareTo(new Long(b.pixelCount()));
        }
    };

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
        return getObjectID() + "_" + getViewType() + "_" + getSequence() ;
    } 


    // if we have a cropped image that fits the exact target box
    // then we return that instead
    private static <I extends Derivative> I processCropPref(List<I> images, I i, long targetWidth, long targetHeight, IMGVIEWTYPE vt, String seq, ImgSearchOpts... opts) {

        // we only process the cropped image preference when we're already looking
        // for primary images with sequence zero
        if (vt == null || !vt.equals(IMGVIEWTYPE.PRIMARY) || seq == null || !seq.equals("0") )
            return i;

        // if user prefers crop
        if (i.isCropped())
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

    public static <I extends Derivative> I getLargestImage(List<I> images, IMGVIEWTYPE vt, String seq) {
        I ip = null;
        if (images != null) {
            for (I i : images) {
                if ( filterMatch(i,vt,seq)) {
                    // skip over any cropped images or images that are too large
                    if (ip == null || i.pixelCount() > ip.pixelCount())
                        ip = i;
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

    private URI getSourceImageURI(String spec) {
        try {
            return new URI(spec);
        }
        catch (URISyntaxException ue) {
            log.error("URISyntaxException when fetching source image URL: ", ue);
            return null;
        }
    }

    public URI getSourceImageURI() {
        return getSourceImageURI(
                getManager().getConfig().getString(imagingServerURLPropertyName) + getRelativeSourceImageURI()
        );
    }

    public URI getRelativeSourceImageURI() {
        return getSourceImageURI(this.getImgVolumePath()+ this.getFilename());
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
    protected void setFilename(String filename) {
        this.filename = filename;
    }

    protected void setFormat(IMGFORMAT format) {
        this.format = format;
    }

    protected void setWidth(Long width) {
        this.width = width;
    }

    protected void setHeight(Long height) {
        this.height = height;
    }

    protected void setTargetWidth(Long targetWidth) {
        this.targetWidth = targetWidth;
    }

    protected void setTargetHeight(Long targetHeight) {
        this.targetHeight = targetHeight;
    }

    protected void setViewType(IMGVIEWTYPE viewType) {
        this.viewType = viewType;
    }

    protected void setSequence(String sequence) {
        this.sequence = sequence;
    }

    protected void setTmsObjectID(Long tmsObjectID) {
        this.tmsObjectID = tmsObjectID;
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
        return getViewType().equals(IMGVIEWTYPE.CROPPED);
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

    private Long tmsObjectID = null;
    public Long getObjectID() {
        return tmsObjectID;
    }
    
    public String getTitle() {
    	if (getObjectID() == null)
    		return null;
    	// return the title of the NGA art object associated with this image
        ArtObject ao = null;
        try {
            ao = getManager().fetchByObjectID(getObjectID());
        } catch (Exception exception) {
            log.error("Can not fetch By Object ID");
        }
        if (ao != null)
    		return "_" + ao.getTitle() + "_";

    	return null;
    }

}


