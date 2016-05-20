package gov.nga.integration.cspace;

public class OrderField {
	public OrderField(String field, boolean ascending) {
		this.fieldName=field;
		this.ascending=ascending;
	}
	String fieldName=null;
	boolean ascending=true;
}

