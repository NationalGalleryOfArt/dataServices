package gov.nga.integration.cspace;

import gov.nga.entities.art.ArtObject;

public class AbridgedObjectRecord extends Record implements NamespaceInterface {
	
	private static final String defaultNamespace = "cultObj";

	public enum PREDICATE {
		HASPARENT("hasParent"),
		HASCHILD("hasChild"),
		HASSIBLING("hasSibling");
		
		private String label;
		public String getLabel() {
			return label;
		}
		
		private PREDICATE(String label) {
			this.label = label;
		};
	};
	
	public AbridgedObjectRecord(ArtObject o) {
		setNamespace("object");
		setSource("tms");
		setId(o.getObjectID().toString());
	}
	
	public static String getDefaultNamespace() {
		return defaultNamespace;
	}
}
