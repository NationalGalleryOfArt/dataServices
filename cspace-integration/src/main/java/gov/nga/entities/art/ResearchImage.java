package gov.nga.entities.art;

import gov.nga.utils.StringUtils;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.converter.ConversionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO - review to be sure this is the
// most suitable pattern
public class ResearchImage extends ArtObjectImage {
    
    private static final Logger log = LoggerFactory.getLogger(ResearchImage.class);
    
    public ResearchImage(ArtDataManagerService manager) {
        super(manager);
    }

    protected static final String fetchAllImagesQuery = 
        "SELECT imageID,        imgVolumePath,      filename,       format, " + 
        "       width,          height,             targetWidth,    targetHeight, " +
        "       viewType,       setDotSequence,     tmsObjectID, " +
        "                       tmsImageObjectID,   projectID, " +
        "       isDetail,       isZoomable,         viewSubType, " +
        "       altCaption,     altAttribution,     altTitle, " +
        "       altDisplayDate, altMedium,          altCreditLine, " +
        "       altImageRef,    qualifier,          photoCredit  " +
        "FROM data.object_researchimages ";

    protected String getAllImagesQuery() {
        return fetchAllImagesQuery;
    }
    
    private ArtObject ngaArtObjectOfImage = null;
    public ArtObject getNGAArtObjectOfImage() {
        if (ngaArtObjectOfImage == null && getTmsImageObjectID() != null) {
            //log.info("==================== tmsimageobjectid:"+getTmsImageObjectID());
            ArtDataManagerService tms = getManager();
            if (tms == null)
                log.error("======================= NULL for getManager()");
            else {
                try {
                    ngaArtObjectOfImage = tms.fetchByObjectID(getTmsImageObjectID());
                } catch (DataNotReadyException exception) {
                    log.error("Can not fetch By Object ID:" + exception.getMessage());
                }
            }
        }
        return ngaArtObjectOfImage;
    }

    public ResearchImage(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
        super(manager, rs);

        String viewType = rs.getString(9);
        // in some cases, a view type is already set by the time it gets here (e.g., for research images that are primary views)
        // the ArtObjectImage class sets the view types it recognizes and defaults to ALTERNATE if none can be found
        if (viewType.equals(Derivative.IMGVIEWTYPE.TECHNICAL.getLabel()))
            setViewType(Derivative.IMGVIEWTYPE.TECHNICAL);
        else if (viewType.equals(Derivative.IMGVIEWTYPE.COMPFIG.getLabel()))
            setViewType(Derivative.IMGVIEWTYPE.COMPFIG);
        else if (viewType.equals(Derivative.IMGVIEWTYPE.INSCRIPTION.getLabel()))
            setViewType(Derivative.IMGVIEWTYPE.INSCRIPTION);
            
        try {
            setTmsImageObjectID(TypeUtils.getLong(rs, 12));
            setProjectID(rs.getString(13));
            setIsDetail(TypeUtils.getLong(rs, 14));
            setIsZoomable(TypeUtils.getLong(rs, 15));
            setViewSubType(rs.getString(16));
            setAltCaption(rs.getString(17));
            setAltAttribution(rs.getString(18));
            setAltTitle(rs.getString(19));
            setAltDisplayDate(rs.getString(20));
            setAltMedium(rs.getString(21));
            setAltCreditLine(rs.getString(22));
            setAltImageRef(rs.getString(23));
            setQualifier(rs.getString(24));
            setPhotoCredit(rs.getString(25));
            
            // because PPCM is usually computed based on other data, it goes last
            // setPixelsPerCM(TypeUtils.getLong(rs, xx));
            
            // no OSCI authors have opted to specify this data on an image
            // by image basis yet, so better safe than sorry
            setPixelsPerCM(null);
        }
        catch (SQLException se) {
            log.error(se.getMessage());
            throw se;
        }
    }

    public Derivative factory(ResultSet rs) throws SQLException {
        ResearchImage d = new ResearchImage(getManager(),rs);
        return d; 
    }
    
    private Long pixelsPerCM;
    public Long getPixelsPerCM() {
        // if we are passed a NULL value, then we calculate PPCM in
        // cases where the research image is a non-detail / non-crop of an NGA object
        // for derivatives, this value might be pretty low and probably isn't very useful
        if ( pixelsPerCM == null && !isDetail() && !isCropped() ) {
            ArtObject o = getNGAArtObjectOfImage();
            if (o != null) {
                try {
                    Measure<Double, Length> m = o.getDimension(ArtObjectDimension.DIMENSION_TYPE.WIDTH);
                    if (m != null) {
                        Double d = m.doubleValue(SI.CENTIMETER);
                        if (d != null && d > 0) {
                            pixelsPerCM = (long) (getWidth() / d);
                        }
                    }
                }
                catch (ConversionException ce) {
                    log.error(ce.getMessage());
                }
            }
        }
        return pixelsPerCM;
    }
    private void setPixelsPerCM(Long pixelsPerCM) {
        this.pixelsPerCM = pixelsPerCM;
    }

    private Long tmsImageObjectID;
    public Long getTmsImageObjectID() {
        return tmsImageObjectID;
    }
    public void setTmsImageObjectID(Long tmsImageObjectID) {
        this.tmsImageObjectID = tmsImageObjectID;
    }

    private String projectID;
    public String getProjectID() {
        return projectID;
    }
    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    private Long isZoomable;
    public boolean getIsZoomable() {
        return TypeUtils.longToBoolean(isZoomable);
    }
    public void setIsZoomable(Long isZoomable) {
        this.isZoomable = isZoomable;
    }

    private Long isDetail;
    public Long getIsDetail() {
        return isDetail;
    }
    public void setIsDetail(Long isDetail) {
        this.isDetail = isDetail;
    }
    public boolean isDetail() {
        return TypeUtils.longToBoolean(getIsDetail());
    }
    

    private String viewSubType;
    public String getViewSubType() {
        return viewSubType;
    }
    public void setViewSubType(String viewSubType) {
        this.viewSubType = viewSubType;
    }

    private String altCaption;
    public String getAltCaption() {
        return altCaption;
    }
    public void setAltCaption(String altCaption) {
        this.altCaption = altCaption;
    }

    private String altAttribution;
    public String getAltAttribution() {
        return altAttribution;
    }
    public void setAltAttribution(String altAttribution) {
        this.altAttribution = altAttribution;
    }

    private String altTitle;
    public String getAltTitle() {
        return altTitle;
    }
    public void setAltTitle(String altTitle) {
        this.altTitle = altTitle;
    }

    private String altDisplayDate;
    public String getAltDisplayDate() {
        return altDisplayDate;
    }
    public void setAltDisplayDate(String altDisplayDate) {
        this.altDisplayDate = altDisplayDate;
    }

    private String altMedium;
    public String getAltMedium() {
        return altMedium;
    }
    public void setAltMedium(String altMedium) {
        this.altMedium = altMedium;
    }

    private String altCreditLine;
    public String getAltCreditLine() {
        return altCreditLine;
    }
    public void setAltCreditLine(String altCreditLine) {
        this.altCreditLine = altCreditLine;
    }

    private String altImageRef;
    public String getAltImageRef() {
        return altImageRef;
    }
    public void setAltImageRef(String altImageRef) {
        this.altImageRef = altImageRef;
    }

    private String qualifier;
    public String getQualifier() {
        return qualifier;
    }
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public IMAGECLASS getImageClass() {
        return IMAGECLASS.RESEARCHIMAGE;
    }
    
    public String getTitle() {
        String title = getAltTitle();
        if (title != null)
            return title;

        // if there's no alt title, return data from the NGA art object
        // that is the subject of this image and mark it with underscores
        // to indicate that it should be formatted with italics when displayed
        ArtObject ao = getNGAArtObjectOfImage(); 
        if (ao != null)
            title = "_" + ao.getTitle() + "_";
        
        return title;
    }
    
    public String getAttribution() {
        String attribution = getAltAttribution();
        if (attribution != null)
            return attribution;

        // if there's no alt attribution, return data from the NGA art object
        // that is the subject of this image if one is set
        ArtObject ao = getNGAArtObjectOfImage();
        if (ao != null)
            return ao.getAttribution();
        return null;
    }

    public String getDisplayDate() {
        String displayDate = getAltDisplayDate();
        if (displayDate != null)
            return displayDate;

        // if there's no alt date, return data from the NGA art object
        // that is the subject of this image
        ArtObject ao = getNGAArtObjectOfImage();
        if (ao != null)
            return ao.getDisplayDate();
        return null;
    }
    
    public String getMedium() {
        String medium = getAltMedium();
        if (medium != null)
            return medium;

        // if there's no alt date, return data from the NGA art object
        // that is the subject of this image
        ArtObject ao = getNGAArtObjectOfImage();
        if (ao != null)
            return ao.getMedium();
        return null;
    }

    public String getCreditLine() {
        String creditLine = getAltCreditLine();
        if (creditLine != null)
            return creditLine;

        // if there's no alt credit line, return data from the NGA art object
        // that is the subject of this image
        ArtObject ao = getNGAArtObjectOfImage();
        if (ao != null) {
            // when a research image appears in a potentially mixed list of
            // other images, we want to ensure the credit line includes a
            // reference to the NGA
            if ( ao.isAccessioned() ) {
                creditLine = "National Gallery of Art, Washington";
                String cLine = ao.getCreditLine();
                if ( cLine != null && cLine.length() > 0 )
                    creditLine += ", " + cLine;
            }
        }
        return creditLine;
    }

    String photoCredit;
    private void setPhotoCredit(String photoCredit) {
        this.photoCredit = photoCredit;
    }
    public String getPhotoCredit() {
        return photoCredit;
    }
    
    private String getAccNumForCaption() {
        ArtObject ao = getNGAArtObjectOfImage();
        if (ao != null && ao.isAccessioned())
            return ao.getAccessionNum();
        return null;
    }

    // there is also an altCaption in the DB, but we don't use it because it was
    // only used for Jen's tracking purposes.  In theory, another customer might wish to
    // use that as a general override, so for now, we'll leave the data in situ, but we
    // might have to clear out the DB entries for the dutch catalogue in the future
    // so that we can use that field as an override
    // also, the format below is probably not exactly correct - I suspect it will need
    // revisiting in the near future
    public String getCaption() {
        String cap = StringUtils.concatIfNotNull(
                ", ",
                getQualifier(),
                getAttribution(),
                getTitle(),
                getDisplayDate(),
                getMedium(),
                getCreditLine()
        );

        cap = StringUtils.concatIfNotNull(
                ". ",
                cap,
                getPhotoCredit()
        );

        cap = StringUtils.concatIfNotNull(
                ", ",
                cap,
                getAccNumForCaption()
        );
        return cap;
    }
    
    public Derivative getAltCompareImage() {
        // first, check to see whether we even have a reference to an alternative image
        String ref = getAltImageRef();
        if (ref == null)
            return null;

        // parse the reference - for compare images, this is colon delimited
        // with first part specifying view type and second 
        String[] parts = ref.split(":");
        if (parts == null || parts.length < 2)
            return null;
        String viewType = parts[0];
        String sequence = parts[1];
        IMGVIEWTYPE vt = IMGVIEWTYPE.toViewType(viewType);
        if (vt == null)
            return null;

        ArtObject ao = getArtObject();
        if (ao == null)
            return null;

        // get the list of research images
        List<ResearchImage> l = ao.getResearchImages();
        if (l != null) {
            ResearchImage ri = ResearchImage.getLargestZoomImage(l, vt, sequence);
            if (ri != null)
                return ri;
        }

        // if we didn't find the alternate compare image in the list of 
        // research images, then search the art object images as well
        List<Derivative> ail = ao.getImages();
        if (ail == null)
            return null;

        Derivative di = Derivative.getLargestZoomImage(ail, vt, sequence);
        return di;
    }

    

}

