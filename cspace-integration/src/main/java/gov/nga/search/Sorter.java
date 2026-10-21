/*
    Sorter implements some complex string comparisons taking into account diacritcs and other nuances of international data sets
  
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
import gov.nga.utils.StringUtils;
import gov.nga.utils.hashcode.CustomHash;

import java.text.CollationKey;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Sorter implements CustomHash { 

	private SortOrder sortOrder = null;
	public static final int NULL=999999999;
	
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
	
	private static Map<String, CollationKey> collationKeyCache = CollectionUtils.newHashMap(); 
	// TODO: this needs to be changed to force the comparison of a collation key which can then be pre-computed 
	// by the caller rather than forcing the collator to use a very expensive method every time
	
	public static <T1 extends Comparable<T1>> int compareObjectsDiacriticalAutoCache(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return Sorter.NULL;
		if (v1 == null)
			return 1;
		if (v2 == null)
			return -1;
		if (v1.equals(v2))
			return Sorter.NULL;
		
		String s1 = v1.toString();
		String s2 = v2.toString();
		CollationKey k1 = collationKeyCache.get(s1);
		CollationKey k2 = collationKeyCache.get(s2);
		if (k1 == null) {
			k1 = StringUtils.getDefaultCollator().getCollationKey(s1);
			collationKeyCache.put(s1, k1);
		}
		if (k2 == null) {
			k2 = StringUtils.getDefaultCollator().getCollationKey(s2);
			collationKeyCache.put(s2, k2);
		}
		return k1.compareTo(k2);
	}

	public static int compareObjectsDiacritical(CollationKey k1, CollationKey k2) {
		if (k1 == null && k2 == null)
			return Sorter.NULL;
		if (k1 == null)
			return 1;
		if (k2 == null)
			return -1;
		if (k1.equals(k2))
			return Sorter.NULL;
		return k1.compareTo(k2);
	}

	public static <T1 extends Comparable<T1>> int compareObjects(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return Sorter.NULL;
		if (v1 == null)
			return 1;
		if (v2 == null)
			return -1;
		if (v1.equals(v2))
			return Sorter.NULL;
		return v1.compareTo(v2);
	}

	protected static <T1 extends Comparable<T1>> int compareMatchesPreferLarger(T1 v1, T1 v2) {
		if (v1 == null && v2 == null)
			return Sorter.NULL;
		if (v1 == null)
			return 1;
		if (v2 == null)
			return -1;
		if (v1.equals(v2))
			return Sorter.NULL;
		// this is key - we have to reverse the sort because we're preferring cases where
		// a larger number of matches exist 
		return v2.compareTo(v1);
	}
	

	
}
