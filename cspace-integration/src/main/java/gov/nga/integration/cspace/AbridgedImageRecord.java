package gov.nga.integration.cspace;

import gov.nga.entities.art.Derivative;

public class AbridgedImageRecord extends AbridgedRecord {
	
	public enum PREDICATE {
		HASPRIMARYDEPICTION("hasPrimaryDepiction"),
		HASDEPICTION("hasDepiction"),
		PRIMARILYDEPICTS("primarilyDepicts"),
		DEPICTS("depicts");
		
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
}