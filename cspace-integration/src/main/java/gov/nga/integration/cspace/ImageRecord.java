/*
    NGA ART DATA API: ImageRecord is the JSON bean representation for Images returned by the APIs

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

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtObjectDimension;
import gov.nga.entities.art.OperatingModeService;

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

	 
@JsonPropertyOrder({ "type", "format", "conforms_to", "iiifURL", "classified_as", "referred_to_by", 
					 "namespace", "source", "id", "mimetype", "classification", "url", "fingerprint", 
					 "width", "height", "title", "lastModified", 
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

    public ImageRecord(CSpaceImage d, boolean references, OperatingModeService om, CSpaceTestModeService ts, List<CSpaceImage> images, String[] urlParts) throws InterruptedException, ExecutionException, MalformedURLException {
    	super(d,references,om,ts,urlParts);
    	if (d == null)
    		return;
    	BeanUtils.copyProperties(d, this);
    	setFilename(d.getFilename());
    	setDescription(d.getDescription());
    	setSubjectWidthCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.WIDTH));
    	setSubjectHeightCM(d.getDimensionOfSubject(ArtObjectDimension.DIMENSION_TYPE.HEIGHT));    	
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