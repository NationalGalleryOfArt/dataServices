package gov.nga.integration.cspace;

public class LinkedArtIdentifier extends LinkedArtClassifiedType {
	
	public LinkedArtIdentifier(String value, LinkedArtClassifiedType... classifications) {
		super("Identifier", null, value, classifications);
	}

}
