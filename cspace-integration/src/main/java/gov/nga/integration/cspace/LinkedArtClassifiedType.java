package gov.nga.integration.cspace;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type", "label", "value", "classified_as"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtClassifiedType extends LinkedArtBaseClass {

	private List<LinkedArtBaseClass> classifiedAs = CollectionUtils.newArrayList();
	
	public LinkedArtClassifiedType(String type, String label, String value, LinkedArtClassifiedType... classifiedAs)  {
		super(type);
		setClassifiedAs(classifiedAs);
		setLabel(label);
		setValue(value);
	}

	// for types that don't need a label since they are exactly described by their classifications only
	public LinkedArtClassifiedType(String type, String value, LinkedArtClassifiedType... classifications)  {
		this(type,null,value,classifications);
	}

	private void setClassifiedAs(LinkedArtClassifiedType[] classifiedAs) {
		if (classifiedAs == null)
			return;
		for (LinkedArtClassifiedType c : classifiedAs)
			addClassifiedAs(c);
	}
	public void addClassifiedAs(LinkedArtBaseClass c) {
		classifiedAs.add(c);
	}
	public List<LinkedArtBaseClass> getClassified_as() {
		if ( classifiedAs != null && classifiedAs.size() > 0 )
			return classifiedAs;
		return null;
	}
	
}

