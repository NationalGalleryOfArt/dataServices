package gov.nga.integration.cspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "context", "id", "type" }) // context TBD
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class LinkedArtBaseClass extends Record {

	private String baseURL = null;
	private String context = null;
	private String type = null;
	private String label = null;
	private String value = null;
	
	public LinkedArtBaseClass(String type) {
		// namespace and source are not used in LinkedArt model 
		setNamespace(null);
		setSource(null);
		setType(type);
	}
	
	@Override
	public String getSource() {
		return null;
	}

	private void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	public String getContext() {
		return this.context;
	}
	
	public String getBaseUrl() {
		return this.baseURL;
	}

	void setBaseUrl(String baseURL) {
		this.baseURL = baseURL;
	}
	
	protected void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}

	protected void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}


}


