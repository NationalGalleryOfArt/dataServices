package gov.nga.search;

import gov.nga.search.SortOrder;

public interface Sortable {
    public Long matchesAspect(Object ao, Object order);
	public Integer aspectScore(Object ao, Object order, String matchString);
	public SortOrder getDefaultSortOrder();
	public SortOrder getNaturalSortOrder();
}
