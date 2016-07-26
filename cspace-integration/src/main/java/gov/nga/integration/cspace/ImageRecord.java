package gov.nga.integration.cspace;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
		subjectWidthCM,
		subjectHeightCM,
		creator,
		originalSource,
		originalSourceType,
		originalFilename,
		projectDescription,
		captureDevice,
		originalSourceInstitution,
		photographer,
		productType;

    public ImageRecord(CSpaceImage d, boolean references, CSpaceTestModeService ts, List<CSpaceImage> images) throws InterruptedException, ExecutionException {
    	super(d,references,ts);
    	if (d == null)
    		return;
    	BeanUtils.copyProperties(d, this);
    	setSequence(d.getSequence());
    	setFilename(d.getFilename());
    	setDescription(d.getDescription());
    	setSubjectWidthCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.WIDTH));
    	setSubjectHeightCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.HEIGHT));    	
    }

	public String getSequence() {
		return sequence;
	}

	private void setSequence(String sequence) {
		this.sequence = sequence;
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

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
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