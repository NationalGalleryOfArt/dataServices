/*
    SortOrder is a container for a list of sort orders to be used by SortHelper
  
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

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SortOrder implements CustomHash { 
	
	private List<Object> sortOrder = null;
	
	public long customHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(3,5);
		if (sortOrder != null) {
			for (Object o : sortOrder) {
				hcb.append(o);
			}
		}
		return hcb.hashCode();
	}
	public List<Object> getSortOrder() {
		return sortOrder;
	}
	
	public SortOrder(List<Object> order) {
		if (order != null)
			sortOrder = CollectionUtils.newArrayList(order);
		else
			sortOrder = null;
	}

	public SortOrder(Object... order) {
		this(Arrays.asList(order));
	}
	
}
