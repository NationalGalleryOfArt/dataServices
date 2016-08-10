package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "namespace", "id", "source", "lastModifiedOn", "fingerprint", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Record {

	private String 
		namespace,
		id,
		fingerprint,
		source,
		lastModified;

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

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references=references;
	};

}
