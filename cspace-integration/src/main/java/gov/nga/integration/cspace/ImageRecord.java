package gov.nga.integration.cspace;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
					 "viewType", "sequence", "filename", "description", "references" })
public class ImageRecord extends AbridgedImageRecord {

	//private static final Logger log = LoggerFactory.getLogger(ObjectRecord.class);
	
    // as far as the CSPACE API is concerned the following fields are ALL optional
    // and only appear with the unabridged version of the cultural object record
    private String sequence;
    private String viewType;
    private String filename;			// we should have this for the most part
    private String description;			// not clear what this would be used for, but listed as optional in CS integration spec - perhaps portfolio?
    
    public ImageRecord(CSpaceImage d) {
    	this(d, true);
    }
       
    public ImageRecord(CSpaceImage d, boolean references) {
    	super(d,references);
    	if (d == null)
    		return;
    	setSequence(d.getSequence());
    	if (d.getViewType() != null)
    		setViewType(d.getViewType().getLabel());
    	setFilename(d.getFilename());
    	setDescription(null);			// no description for this type of image record
    }

	public String getSequence() {
		return sequence;
	}

	private void setSequence(String sequence) {
		this.sequence = sequence;
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

}