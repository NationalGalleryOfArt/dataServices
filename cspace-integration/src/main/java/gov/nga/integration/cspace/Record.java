package gov.nga.integration.cspace;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "namespace", "id", "source", "lastModifiedOn", "fingerprint", "references" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Record implements NamespaceInterface {
	
	private String namespace;
	private String id;
	private String fingerprint;
	private String source;
	private String lastModified;
	private URL url;

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
	protected void setSource(String source) {
//		if ( this.source != null)
//			log.info("source was: " + this.source + " and changing to it to: " + source + " for class: " + this.getClass().getName());
//		else
//			log.info("source is null and changing to it to: " + source + " for class: " + this.getClass().getName());
		this.source = source;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}

	// can set a different JsonProperty name with this annotation if we want to
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
