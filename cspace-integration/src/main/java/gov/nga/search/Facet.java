/*
    Facet represents a search facet
  
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.utils.MutableInt;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import gov.nga.utils.CollectionUtils;
 
public class Facet {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Facet.class);

	public static final String NOVALUEKEY="zz_no_value";
	
	// the facet comparator sorts using collation
	// as well as preferring alphas over numerics
	public static Comparator<String> facetSorter = new Comparator<String>() {
		public int compare(String a, String b) {
			// should never match here because we're now assigning
			// novaluekey when a or b equals empty string
			/*if (a.equals(""))
				a = null;
			if (b.equals(""))
				b = null;
			*/
			//log.warn(a + " vs " + b);
			if (a == null && b == null)
				return 0;
			if (a == null && b != null)
				return 1;
			if (b == null && a != null)
				return -1;
			
			boolean aNumeric = Character.isDigit(a.charAt(0));
			boolean bNumeric = Character.isDigit(b.charAt(0));
			if (aNumeric && !bNumeric)
				return 1;
			if (bNumeric && !aNumeric)
				return -1;
			int i = Sorter.compareObjectsDiacriticalAutoCache(a, b);
			return i == Sorter.NULL ? 0 : i;
		}
	};
	
	private Map<String, MutableInt> facetHolder = CollectionUtils.newHashMap();
	private Map<String, Integer> facetCounts = null;

	private Object facet = null;
	
	public Map<String, Integer> getFacetCounts() {
		if (facetCounts == null && facetHolder != null) {
			Map<String, Integer> m = CollectionUtils.newTreeMap(facetSorter);
			for (String s : facetHolder.keySet()) {
				MutableInt mi = facetHolder.get(s);
				m.put(s, Integer.valueOf(mi.get()));
			}
			synchronized(this) {
				facetCounts = m;
			}
		}
		return facetCounts;
	}
	 
	public Object getFacet() {
		return facet;
	}
	
	public Facet(Object f) {
		facet = f;
	}
	
	public void processEntity(Faceted a) {
		List<String> l = a.getFacetValue(getFacet());
		//log.debug(facet + " (" + a.getClass().getCanonicalName() + "): " + l);
		if (l == null)
			l = CollectionUtils.newArrayList();
		// if the list is empty, add a single null value so-as not to lose track
		// of nulls
		if (l.size() == 0)
			l.add(null);
		for (String s : l) {
			if (s == null || s.equals(""))
				s = NOVALUEKEY;
			synchronized (facetHolder) {
				MutableInt cnt = facetHolder.get(s);
				if (cnt == null) {
					facetHolder.put(s, new MutableInt(1));
				}
				else 
					cnt.inc();
			}
		}
	}

}
