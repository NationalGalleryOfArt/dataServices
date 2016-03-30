package gov.nga.search;

public class ResultsPaginator {
	
	private Integer pagesize = 40;
	private Integer page = 1;
	private Integer totalResults = null;
	private Integer startIndex = null;
	private Integer endIndex = null;
	
	public ResultsPaginator(Integer pagesize, Integer page) {
		setPageSize(pagesize);
		setPage(page);
	}
	
	public void setTotalResults(Integer l) {
		totalResults = l;
		setPage(page);
	}
	
	public Integer getTotalResults() {
		return totalResults;
	}

	private void setPageSize(Integer l) {
		pagesize = l;
		if (pagesize < 1)
			pagesize = 1;
	}
	
	public Integer getPageSize() {
		return pagesize;
	}
	
	public Integer getPages() {
		if (getTotalResults() != null && getPageSize() != null && getPageSize() > 0) {
			return (int) Math.ceil((double) getTotalResults() / (double) getPageSize());
		}
		return null;
	}

	private void setPage(Integer l) {
		page = l;
		if (page != null) {
			if (getPages() != null && page > getPages())
				page = getPages();
			if (page < 1 && getTotalResults() != null && getTotalResults() > 0)
				page = 1;
		}
		else
			page = 1;
		
		// not ready to calculate start and end indexes unless we have already been informed of
		// the total number of results
		if (getTotalResults() == null)
			return;
		
		// now calculate where we need to grab results from within the result set
		startIndex = (getPage() - 1) * getPageSize();
		endIndex = startIndex + getPageSize() - 1;
		
		// the call to subList is exclusive for endIndex so we need to
		// increment it
		endIndex++;
		
		// swap values if start and end are swapped
		if (startIndex > endIndex) {
			int hold = startIndex;
			startIndex = endIndex;
			endIndex = hold;
		}

		// we cannot specify an index greater than the end of the index of the last item + 1
		if (endIndex > getTotalResults())
			endIndex = getTotalResults();
		if (startIndex < 0)
			startIndex = 0;
	}
	
	public Integer getStartIndex() {
		return startIndex;
	}
	
	public Integer getEndIndex() {
		return endIndex;
	}
	
	public Integer getPage() {
		return page;
	}

}
