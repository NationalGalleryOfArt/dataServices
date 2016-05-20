package gov.nga.integration.cspace;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.Derivative;

@JsonPropertyOrder({ "namespace", "source", "id", "mimetype", "classification", "width", "height", "title", "lastModified", 
					 "viewType", "sequence", "references" })
public class ImageRecord extends AbridgedImageRecord {

	//private static final Logger log = LoggerFactory.getLogger(ObjectRecord.class);
	
    // as far as the CSPACE API is concerned the following fields are ALL optional
    // and only appear with the unabridged version of the cultural object record
    private String sequence;
    private String viewType;
    
    public ImageRecord(Derivative d) {
    	this(d, true);
    }
       
    public ImageRecord(Derivative d, boolean references) {
    	super(d,references);
    	if (d == null)
    		return;
    	setSequence(d.getSequence());
    	setViewType(d.getViewType().getLabel());
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

}