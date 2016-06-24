package gov.nga.integration.cspace;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObjectDimension;

/*
 * according to: https://cs-dev.sirmaplatform.com/emf/service/integrations/dam/model
 * mandatory
 * 	image:id
 *  image:lastModified
 *  image:title
 *  image:classification
 *  image:source
 * 
 * optional
 * 	image:filename
 *  image:description
 *  image:mimetype
*/

	 
@JsonPropertyOrder({ "namespace", "source", "id", "mimetype", "classification", "width", "height", "title", "lastModified", 
					 "viewType", "partner2ViewType", "sequence", "filename", "description", "subjectWidthCM", "subjectHeightCM", 
					 "originalSource", "originalSourceType", "originalFilename", "projectDescription", "lightQuality",
					 "spectrum", "captureDevice", "originalSourceInstitution", "photographer", "productionDate", "creator", 
					 "viewDescription", "treatmentPhase", "productType", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageRecord extends AbridgedImageRecord {

	//private static final Logger log = LoggerFactory.getLogger(ObjectRecord.class);

    // as far as the CSPACE API is concerned the following fields are ALL optional
    // and only appear with the unabridged version of the cultural object record
	private String
		sequence,
		viewType,
		partner2ViewType,
		filename,				// we should have this for the most part
		description,			// not clear what this would be used for, but listed as optional in CS integration spec - perhaps portfolio?
		subjectWidthCM,
		subjectHeightCM,
		creator,
		originalSource,
		originalSourceType,
		originalFilename,
		viewDescription,
		projectDescription,
		lightQuality,
		spectrum,
		treatmentPhase,
		captureDevice,
		originalSourceInstitution,
		photographer,
		productionDate,
		productType;
    
    public ImageRecord(CSpaceImage d, boolean references, CSpaceTestModeService ts) {
    	super(d,references,ts);
    	if (d == null)
    		return;
    	BeanUtils.copyProperties(d, this);
    	setSequence(d.getSequence());
    	if (d.getViewType() != null) {
    		if (ts.isTestModeOtherHalfObjects())
    			partner2ViewType = d.getViewType().getLabel();
    		else
    			setViewType(d.getViewType().getLabel());
    	}
    	setFilename(d.getFilename());
    	setDescription(null);			// no description for this type of image record
    	setSubjectWidthCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.WIDTH));
    	setSubjectHeightCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.HEIGHT));    	
    }

	public String getSequence() {
		return sequence;
	}

	private void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public String getPartner2ViewType() {
		return partner2ViewType;
	}

	public String getViewType() {
		return viewType;
	}

	private void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubjectWidthCM() {
		return subjectWidthCM;
	}

	public void setSubjectWidthCM(Double subjectWidthCM) {
		if (subjectWidthCM != null)
			this.subjectWidthCM = subjectWidthCM.toString();
	}

	public String getSubjectHeightCM() {
		return subjectHeightCM;
	}

	public void setSubjectHeightCM(Double subjectHeightCM) {
		if (subjectHeightCM != null)
			this.subjectHeightCM = subjectHeightCM.toString();
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getOriginalSource() {
		return originalSource;
	}

	public void setOriginalSource(String originalSource) {
		this.originalSource = originalSource;
	}

	public String getOriginalSourceType() {
		return originalSourceType;
	}

	public void setOriginalSourceType(String originalSourceType) {
		this.originalSourceType = originalSourceType;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getViewDescription() {
		return viewDescription;
	}

	public void setViewDescription(String viewDescription) {
		this.viewDescription = viewDescription;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getLightQuality() {
		return lightQuality;
	}

	public void setLightQuality(String lightQuality) {
		this.lightQuality = lightQuality;
	}

	public String getSpectrum() {
		return spectrum;
	}

	public void setSpectrum(String spectrum) {
		this.spectrum = spectrum;
	}

	public String getTreatmentPhase() {
		return treatmentPhase;
	}

	public void setTreatmentPhase(String treatmentPhase) {
		this.treatmentPhase = treatmentPhase;
	}

	public String getCaptureDevice() {
		return captureDevice;
	}

	public void setCaptureDevice(String captureDevice) {
		this.captureDevice = captureDevice;
	}

	public String getOriginalSourceInstitution() {
		return originalSourceInstitution;
	}

	public void setOriginalSourceInstitution(String originalSourceInstitution) {
		this.originalSourceInstitution = originalSourceInstitution;
	}

	public String getPhotographer() {
		return photographer;
	}

	public void setPhotographer(String photographer) {
		this.photographer = photographer;
	}

	public String getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public void setSubjectWidthCM(String subjectWidthCM) {
		this.subjectWidthCM = subjectWidthCM;
	}

	public void setSubjectHeightCM(String subjectHeightCM) {
		this.subjectHeightCM = subjectHeightCM;
	}


}