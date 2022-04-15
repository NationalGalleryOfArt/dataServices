package gov.nga.search;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

//public class FreeTextFilter<C extends FreeTextSearchable<C>> {
public class FreeTextFilter implements CustomHash {

	private List<Object> fieldsToSearch = CollectionUtils.newArrayList();
	private String searchTerm = null;

	public long customHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(17,19);
		hcb.append(searchTerm);
		for (Object o : fieldsToSearch) {
			hcb.append(o);
		}
		return hcb.hashCode();
	}
	
	public <F> FreeTextFilter(F fieldToSearch, String searchTerm) {
		this.fieldsToSearch.add(fieldToSearch);
		this.searchTerm = searchTerm;
	} 

	public <F> FreeTextFilter(List<F> fieldsToSearch, String searchTerm) {
		for (F f : fieldsToSearch) {
			this.fieldsToSearch.add(f);
		}
		this.searchTerm = searchTerm;
	}
	
	public List<Object> getFields() {
		return fieldsToSearch;
	}
	
	public String getSearchTerm() {
		return searchTerm;
	}
	
}
