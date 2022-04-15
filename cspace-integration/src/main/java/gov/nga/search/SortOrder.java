package gov.nga.search;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

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
