package gov.nga.integration.cspace;



import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtInformationObject extends LinkedArtRecord {
	
	private List<LinkedArtBaseClass> about = CollectionUtils.newArrayList();

	public LinkedArtInformationObject() {
		super("InformationObject");
	}
	
	public void addAbout(LinkedArtBaseClass l) {
		this.about.add(l);
	}
	
	public List<LinkedArtBaseClass> getAbout() {
		if (about != null && about.size() > 0)
			return about;
		return null;
	}
	
	
//	public LinkedArtInformationObject(String label, String value) {
//		super("InformationObject", label, value);
//	}

}

