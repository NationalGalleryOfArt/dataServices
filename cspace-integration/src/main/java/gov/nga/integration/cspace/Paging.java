package gov.nga.integration.cspace;

import gov.nga.search.ResultsPaginator;

public class Paging {
	int limit;
	int skip;
	int total;
	
	public Paging(ResultsPaginator pn) {
		setSkip(pn.getStartIndex());
		setLimit(pn.getPageSize());
		setTotal(pn.getTotalResults());
	}

	public void setLimit(Integer limit) {
		if (limit != null)
			this.limit = limit;
	}

	public void setSkip(Integer skip) {
		if (skip != null)
			this.skip = skip;
		else
			this.skip = 0;
	}

	public void setTotal(Integer total) {
		if (total != null)
			this.total = total;
		else
			this.total = 0;
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
