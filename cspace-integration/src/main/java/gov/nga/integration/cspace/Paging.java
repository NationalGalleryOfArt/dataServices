package gov.nga.integration.cspace;

import gov.nga.search.ResultsPaginator;

public class Paging {
	long limit;
	long skip;
	long total;
	
	public Paging(ResultsPaginator pn) {
		setSkip(pn.getStartIndex());
		setLimit(pn.getPageSize());
		setTotal(pn.getTotalResults());
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public void setSkip(long skip) {
		this.skip = skip;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getLimit() {
		return limit;
	}

	public long getSkip() {
		return skip;
	}

	public long getTotal() {
		return total;
	}
}
