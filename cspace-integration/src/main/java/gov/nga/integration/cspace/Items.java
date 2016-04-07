package gov.nga.integration.cspace;

import java.util.List;

import gov.nga.search.ResultsPaginator;

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
