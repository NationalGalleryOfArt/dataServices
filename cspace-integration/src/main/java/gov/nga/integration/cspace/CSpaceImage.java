package gov.nga.integration.cspace;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;

import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtObject;
import gov.nga.entities.art.ArtObjectImage;

import gov.nga.imaging.Thumbnail;

public abstract class CSpaceImage extends ArtObjectImage {

    private static final String CLASSIFICATION = "image";
	public CSpaceImage(ArtDataManagerService manager) {
        super(manager);
        setClassification(CSpaceImage.CLASSIFICATION);
    }
    
    public CSpaceImage(ArtDataManagerService manager, ResultSet rs) throws SQLException  {
        super(manager, rs); 
        setClassification(CSpaceImage.CLASSIFICATION);
    }

    // return a thumbnail as close as possible to the desired size (or exact size only if an exact size is required
    // send back a base64 encoded stream of bytes if base64 is preferred and feasible to do so 
    // or return a URL to a thumbnail if one is already created
    // otherwise return NULL  
    public abstract Thumbnail getThumbnail(int width, int height, int maxdim, boolean exactSizeRequired, boolean preferBase64, String scheme);

    String classification;
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification=classification;
	}
    
	// From Roya who will have to provide better specifications than this:
	// The artist is needed to be part of the Name(link) in order for Conservators to recognize the image. 
	// if you do the query for artist: Matisse and title: La Negress, you will get 516 hit which 514 are the same object 
	// number, and according to DCLPA not useful at all. 
	// the mimestype and the size is not important for them at the first glance.
    @Override
    public String getTitle() {
    	ArtObject o = getArtObject();
    	if (o != null)
    		return o.getAttribution() + "; " + o.getAccessionNum();
   		return null;
    }

//	@Override
//    public String getTitle() {
//    	ArtObject o = getArtObject();
//    	String objectDescription = "";
//    	if (o != null)
//    		objectDescription = "of " + o.getAccessionNum();
//    	
//    	IMGFORMAT f = getFormat();
//    	String format = "unrecognized format";
//    	if (f != null)
//    		format = f.getMimetype();
//    	
//    	IMGVIEWTYPE vt = getViewType();
//    	String view = getViewDescription();
//    	if (vt != null) {
//    		view = vt.getLabel();
//    		if (getSequence() != null)
//    			view += " #" + getSequence();
//    	}
//    	
//    	Long w = getWidth();
//    	Long h = getHeight();
//    	String width = "?";
//    	String height = "?";
//    	if (w != null)
//    		width = w.toString();
//    	if (h != null)
//    		height = h.toString();
//   		return String.format("%s %s [%s %sx%s]", format, objectDescription, view, width, height);
//    }
	
	int thumbnailSize;
	public int getThumbnailSize() {
		return thumbnailSize;
	}
	public void setThumbnailSize(int thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
	}

	String creator;
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

	String originalSource;
	public String getOriginalSource() {
		return originalSource;
	}
	public void setOriginalSource(String originalSource) {
		this.originalSource = originalSource;
	}

	String originalSourceType;
	public String getOriginalSourceType() {
		return originalSourceType;
	}
	public void setOriginalSourceType(String originalSourceType) {
		this.originalSourceType = originalSourceType;
	}

	String originalFilename;
	public String getOriginalFilename() {
		return originalFilename;
	}
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	String viewDescription;
	public String getViewDescription() {
		return viewDescription;
	}
	public void setViewDescription(String viewDescription) {
		this.viewDescription = viewDescription;
	}

	String description; 
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	String projectDescription;
	public String getProjectDescription() {
		return projectDescription;
	}
	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	String lightQuality;
	public String getLightQuality() {
		return lightQuality;
	}
	public void setLightQuality(String lightQuality) {
		this.lightQuality = lightQuality;
	}

	String spectrum;
	public String getSpectrum() {
		return spectrum;
	}
	public void setSpectrum(String spectrum) {
		this.spectrum = spectrum;
	}

	String treatmentPhase;
	public String getTreatmentPhase() {
		return treatmentPhase;
	}
	public void setTreatmentPhase(String treatmentPhase) {
		this.treatmentPhase = treatmentPhase;
	}

	String captureDevice;
	public String getCaptureDevice() {
		return captureDevice;
	}
	public void setCaptureDevice(String captureDevice) {
		this.captureDevice = captureDevice;
	}
	
	String originalSourceInstitution;
	public void setOriginalSourceInstitution(String originalSourceInstitution) {
		this.originalSourceInstitution=originalSourceInstitution;
	}
	public String getOriginalSourceInstitution() {
		return originalSourceInstitution;
	}

	String photographer;
	public void setPhotographer(String photographer) {
		this.photographer=photographer;
	}
	public String getPhotographer() {
		return photographer;
	}

	String productType;
	public void setProductType(String productType) {
		this.productType=productType;
	}
	public String getProductType() {
		return productType;
	}
	
	String productionDate;
	protected void setProductionDate(Date productionDate) {
    	if (productionDate != null) {
    		DateTime dt = new DateTime(productionDate);
    		setProductionDate(dt.toString());
    	}
	}
	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}
	public String getProductionDate() {
		return productionDate;
	}
}
