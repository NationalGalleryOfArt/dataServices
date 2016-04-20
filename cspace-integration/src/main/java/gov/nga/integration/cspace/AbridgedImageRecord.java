package gov.nga.integration.cspace;

import gov.nga.entities.art.Derivative;

public class AbridgedImageRecord extends Record implements NamespaceInterface {
	
	public static final String defaultNamespace = "image";
	
	public enum PREDICATE {
		HASPRIMARYDEPICTION("hasPrimaryDepiction"),
		HASDEPICTION("hasDepiction"),
		PRIMARILYDEPICTS("primarilyDepicts"),
		DEPICTS("depicts"),
		// TODO - implement related assets in order to test conservation space's import features
		RELATEDASSET("relatedAsset");
		
		private String label;
		public String getLabel() {
			return label;
		}
		
		private PREDICATE(String label) {
			this.label = label;
		};
	};
	
	public AbridgedImageRecord(Derivative d) {
		setNamespace("image");
		setSource("web-images-repository");
		setId(d.getImageID());
	}
	
	public static String getDefaultNamespace() {
		return defaultNamespace;
	}
}