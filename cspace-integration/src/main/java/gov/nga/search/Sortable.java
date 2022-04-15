package gov.nga.search;


public interface Sortable {
	// matchesAspect is used for comparisons of two entities (a & b) given
	// with respect to a third entity (ao) rather than direct
	// comparisons between a & b themselves
    public Long matchesAspect(Object ao, Object order);
    
    // used for sorting two objects with respect to some ordering criteria
	public int aspectScore(Object ao, Object order, String matchString);
	
	// the default (preferred) sort order
	public SortOrder getDefaultSortOrder();
	
	// a natural sort order for the entity (preferably fast)
	public SortOrder getNaturalSortOrder();
}
