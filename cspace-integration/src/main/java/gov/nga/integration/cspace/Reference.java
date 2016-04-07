package gov.nga.integration.cspace;

public class Reference {
	private String predicate;
	private Record object;
	
	public Reference(String predicate, Record object) {
		setPredicate(predicate);
		setObject(object);
	}
	
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public Record getObject() {
		return object;
	}
	public void setObject(Record object) {
		this.object = object;
	}
}
