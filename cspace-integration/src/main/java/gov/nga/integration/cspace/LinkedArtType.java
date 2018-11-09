package gov.nga.integration.cspace;

public class LinkedArtType extends LinkedArtClassifiedType {
	
	public LinkedArtType(String label, String value, LinkedArtClassifiedType... classifications) {
		super("Type", label, value, classifications);
	}

	public LinkedArtType(String id, String label, String value, LinkedArtClassifiedType... classifications) {
		this(label, value, classifications);
		setId(id);
	}

}
