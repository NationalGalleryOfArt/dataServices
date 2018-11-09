package gov.nga.integration.cspace;

public class LinkedArtLinguisticObject extends LinkedArtClassifiedType {
	
	public LinkedArtLinguisticObject(String value, LinkedArtClassifiedType... classifications) {
		super("LinguisticObject", null, value, classifications);
	}
	
	public LinkedArtLinguisticObject(String label, String value, LinkedArtClassifiedType... classifications) {
		super("LinguisticObject", label, value, classifications);
	}


}
