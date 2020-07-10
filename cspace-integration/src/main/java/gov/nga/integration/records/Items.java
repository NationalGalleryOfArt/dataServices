package gov.nga.integration.records;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.integration.cspace.Paging;
import gov.nga.search.ResultsPaginator;

@JsonPropertyOrder({ "paging", "items" })
public class Items<T extends ResultItem> {
	Paging paging=null;
	List<T> items=null;
	
	public Items(ResultsPaginator resultsPaginator, List<T> items) {
		this.items=items;
		setPaging(resultsPaginator);
	}
	
	public Paging getPaging() {
		return paging;
	}

	public void setPaging(ResultsPaginator resultsPaginator) {
		this.paging = new Paging(resultsPaginator);
	}
	
	public List<T> getItems() {
		return this.items;
	}

}
