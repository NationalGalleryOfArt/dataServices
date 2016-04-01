package gov.nga.integration.cspace;

public class Reference {
	private String predicate;
	private AbridgedRecord object;
	
	public Reference(String predicate, AbridgedRecord object) {
		setPredicate(predicate);
		setObject(object);
	}
	
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public AbridgedRecord getObject() {
		return object;
	}
	public void setObject(AbridgedRecord object) {
		this.object = object;
	}
}
