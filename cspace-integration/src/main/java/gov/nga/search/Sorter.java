package gov.nga.search;

import gov.nga.utils.StringUtils;
import gov.nga.utils.hashcode.CustomHash;
 
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Sorter implements CustomHash { 

	private SortOrder sortOrder = null;
	
	public long customHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(5,7);
		if (sortOrder != null)
			hcb.append(sortOrder.customHash());
		return hcb.hashCode();
	}
	
	public Sorter() {
		super();
	}
	
	public Sorter(Object... order) {
		this();
		setSortOrder(order);
	}
	
	public Sorter(SortOrder so) {
		this();
		sortOrder = so;
	}
	
	public void setSortOrder(Object... order) {
		sortOrder = new SortOrder(order);
	}
	
	public List<Object> getSortOrder() {
		if (sortOrder != null)
			return sortOrder.getSortOrder();
		return null;
	}
	
	public static <T1 extends Comparable<T1>> Integer compareObjectsDiacritical(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return null;
		if (v1 == null)
			return new Integer(1);
		if (v2 == null)
			return new Integer(-1);
		if (v1.equals(v2))
			return null;
		return StringUtils.getDefaultCollator().compare(v1, v2);
	}

	public static <T1 extends Comparable<T1>> Integer compareObjects(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return null;
		if (v1 == null)
			return new Integer(1);
		if (v2 == null)
			return new Integer(-1);
		if (v1.equals(v2))
			return null;
		return v1.compareTo(v2);
	}

	protected static <T1 extends Comparable<T1>> Integer compareMatchesPreferLarger(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return null;
		if (v1 == null)
			return new Integer(1);
		if (v2 == null)
			return new Integer(-1);
		if (v1.equals(v2))
			return null;
		// this is key - we have to reverse the sort because we're preferring cases where
		// a larger number of matches exist 
		return v2.compareTo(v1);
	}
	

	
}
