package gov.nga.integration.cspace;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reference {
	private String[] predicates;
	private String predicate;
	private Record object;
	
	public Reference(String[] predicate, Record object) {
		setPredicates(predicate);
		setObject(object);
	}
	
	public String[] getPredicates() {
		return predicates;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicates(String[] predicate) {
		// skip setting predicates for now to ensure backwards compatibility
		//this.predicates = predicate;
		
		// for backwards compatibility, also set the "predicate" value
		// if the predicate namespace starts with "cspace:" and remove
		// the predicate namespace
		if (predicate != null) {
			this.predicate = predicate[0].replaceAll("cspace:", ""); 
			/* for (String p : predicate) {
				if ( p.startsWith("cspace:") ) {
					this.predicate = p.replaceAll("cspace:", ""); 
				}
			}
			*/
		}
	}
	public Record getObject() {
		return object;
	}
	public void setObject(Record object) {
		this.object = object;
	}
}
