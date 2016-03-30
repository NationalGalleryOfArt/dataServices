package gov.nga.search;

import java.util.List;

// public interface FreeTextSearchable<T extends FreeTextSearchable<T>> {
public interface FreeTextSearchable<T extends Faceted & Searchable & Sortable> {
	public List<T> freeTextSearch(List<Object> fields, String searchTerm, List<T> baseList);
}
