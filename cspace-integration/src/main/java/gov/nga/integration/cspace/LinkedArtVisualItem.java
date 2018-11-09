package gov.nga.integration.cspace;



import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// needs to support the following formats
// IIIF Images
//"representation": [
//                   {
//                     "id": "http://iiif.example.org/image/1", 
//                     "type": "VisualItem", 
//                     "label": "IIIF Image API for Sculpture", 
//                     "conforms_to": {"id": "http://iiif.io/api/image"}
//                   }
//                 ]
//                 // along with primary image or preferred depiction or something like that
//
//
// NON-IIIF Images


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.Derivative.IMGFORMAT;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtVisualItem extends LinkedArtRecord {
	
	private List<LinkedArtBaseClass> conforms_to = CollectionUtils.newArrayList();

	public LinkedArtVisualItem() {
		super("VisualItem");
	}

	public LinkedArtVisualItem(String id, String label, LinkedArtClassifiedType... classifiedTypes) {
		super("VisualItem", label);
		setId(id);
		if (classifiedTypes != null) {
			for ( LinkedArtClassifiedType c : classifiedTypes) {
				addClassifiedAs(c);
			}
		}
	}
	
	private static Pattern suffixPattern = Pattern.compile("\\.\\w*$");
	public String getFormat() {
		String ext = null;
		String id = getId();
		Matcher m = suffixPattern.matcher(id);
		if ( m.find() ) {
			ext = m.group(0);
			return IMGFORMAT.formatFromExtension(ext).getMimetype();
		}
		else 
			return null;
	}

	public void addConforms_to(LinkedArtBaseClass l) {
		this.conforms_to.add(l);
	}
	
	public List<LinkedArtBaseClass> getConforms_to() {
		if (conforms_to!= null && conforms_to.size() > 0)
			return conforms_to;
		return null;
	}
	

}

