/*
    ResultsPaginator - used to paginate through result sets
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package gov.nga.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultsPaginator {
	
	private Integer pagesize = 40;
	private Integer page = 1;
	private Integer totalResults = null;
	private Integer startIndex = null;
	private Integer endIndex = null;
	private Integer skip = null;
	
	private static final Logger log = LoggerFactory.getLogger(ResultsPaginator.class);
	static {
		log.debug("class loaded");
	}

	public ResultsPaginator() {
	}
	
	public ResultsPaginator(Integer pagesize, Integer page) {
		this();
		setPageSize(pagesize);
		setPage(page);
	}

	public ResultsPaginator(int skip, int limit) {
		this();
		setSkip(skip);
		setPageSize(limit);
	}

	public void setTotalResults(Integer l) {
		totalResults = l;
		if (skip == null)
			setPage(page);
		else
			setWindow(skip, getPageSize());
	}
	
	public Integer getTotalResults() {
		return totalResults;
	}

	protected void setPageSize(Integer l) {
		pagesize = l;
		if (pagesize < 1)
			pagesize = 1;
	}
	
	protected void setSkip(Integer l) {
		this.skip = l;
		if (this.skip < 0)
			this.skip = 0;
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

	protected void setIndexes(Integer startIndex, Integer endIndex) {
		// swap values if start and end are swapped
		if (startIndex > endIndex) {
			int hold = startIndex;
			startIndex = endIndex;
			endIndex = hold;
		}

		// we cannot specify an index greater than the end of the index of the last item + 1
		if (endIndex > getTotalResults())
			endIndex = getTotalResults();
		if (startIndex >= getTotalResults())
			startIndex = getTotalResults();
		if (startIndex < 0)
			startIndex = 0;
		if (endIndex < 1)
			endIndex = 1;
		if (getTotalResults() <= 0 && endIndex == 1)
			endIndex = 0;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	private void setPage(Integer l) {
		// we're changing these locally then validating via setIndex since other subclasses will share
		// this validation now, e.g. ResultsWindow
		Integer startIndex = getStartIndex();
		Integer endIndex = getEndIndex();
		
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
		
		setIndexes(startIndex, endIndex);
	}

	// allow a custom view into the result set to be used - this assumes total external control
	// over the pagination if pagination will be used since calling this will throw off the
	// page calculations a bit
	private void setWindow(Integer skip, Integer limit) {
		setPageSize(limit);
		setIndexes(skip, skip+getPageSize());
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
