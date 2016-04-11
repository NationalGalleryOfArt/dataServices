package gov.nga.integration.cspace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.search.ResultsPaginator;

@JsonPropertyOrder({ "paging", "items" })
public class Items {
	Paging paging=null;
	List<Item> items=null;
	
	public Items(ResultsPaginator resultsPaginator, List<Item> items) {
		this.items=items;
		setPaging(resultsPaginator);
	}
	
	public Paging getPaging() {
		return paging;
	}

	public void setPaging(ResultsPaginator resultsPaginator) {
		this.paging = new Paging(resultsPaginator);
	}
	
	public List<Item> getItems() {
		return this.items;
	}
}
