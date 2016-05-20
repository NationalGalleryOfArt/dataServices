package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "namespace", "id", "source", "lastModifiedOn", "references" })
public abstract class Record {

	private String namespace;
	private String id;
	private String source;
    private String lastModified;

    // if null, don't include it since we have an option in the search APIs to not include references at all - so we don't want to 
	// make it appear as if there are no references - we just won't include them at all if they are null
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<Reference> references;

	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	// can set a different JsonProperty name with
	// @JsonProperty("lastModifiedOn")
	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references=references;
	};

}
