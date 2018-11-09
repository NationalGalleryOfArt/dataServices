package gov.nga.integration.cspace;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "id", "type", "label", "source", "classified_as", "identified_by", "referred_to_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtRecord extends LinkedArtClassifiedType {

	private List<LinkedArtBaseClass> referredToBy = CollectionUtils.newArrayList();
	private List<LinkedArtBaseClass> identities = CollectionUtils.newArrayList();
	private List<LinkedArtBaseClass> representation = CollectionUtils.newArrayList();

	public LinkedArtRecord(String type)  {
		this(type, null, null);
	}

	public LinkedArtRecord(String type, String label)  {
		this(type, label, null);
	}

	public LinkedArtRecord(String type, String label, String value)  {
		super(type, label, value);
		referredToBy = CollectionUtils.newArrayList();
		identities = CollectionUtils.newArrayList();
	}


	protected void addReferredToBy(LinkedArtBaseClass o) {
		referredToBy.add(o);
	}
	public List<LinkedArtBaseClass> getReferred_to_by() {
		if (referredToBy != null && referredToBy.size() > 0)
			return referredToBy;
		return null;
	}

	protected void addIdentity(LinkedArtBaseClass o) {
		identities.add(o);
	}
	public List<LinkedArtBaseClass> getIdentified_by() {
		if (identities != null && identities.size() > 0)
			return identities;
		return null;
	}

	protected void addRepresentation(LinkedArtBaseClass o) {
		representation.add(o);
	}
	public List<LinkedArtBaseClass> getRepresentation() {
		if (representation != null && representation.size() > 0)
			return representation;
		return null;
	}

}

