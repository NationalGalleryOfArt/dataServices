/*
    NGA ART DATA API: Paging holds the pagination properties associated with a window into a particular search result.

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
package gov.nga.integration.cspace;

import gov.nga.common.search.ResultsPaginator;

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
