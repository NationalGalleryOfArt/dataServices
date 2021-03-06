/*
    NGA Art Data API: The comparator implementation for sorting lists of art objects, 
    first by artist, and then by title, taking into account the presence of diacritical 
    forms of characters in names and titles.

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
package gov.nga.entities.art;

import gov.nga.search.SortHelper;
import gov.nga.search.Sorter;

import java.util.Comparator;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ArtObjectMapComparator<T extends ArtObject> implements Comparator<T> {

//	private static final Logger log = LoggerFactory.getLogger(ArtObjectMapComparator.class);

	Map<T, Long> base = null;

	public ArtObjectMapComparator(Map<T, Long> base) {
		this.base = base;
	}

	// since we're comparing the values and not the keys, we cannot return
	// 0 or the map will overwrite the other entry with this value, so
	// we have to compare some other values as a fallback, ultimately
	// ending with unique values that can be differentiated properly
	public int compare(ArtObject a, ArtObject b) {
		int c = base.get(b).compareTo(base.get(a)); 
		if (c == 0) {
			int d = SortHelper.compareObjectsDiacritical(a.getAttributionInvertedCKey(), b.getAttributionInvertedCKey());
			if (d == Sorter.NULL || d == 0) {
				int e = SortHelper.compareObjectsDiacritical(a.getStrippedTitleCKey(), b.getStrippedTitleCKey());
				if (e == Sorter.NULL || e == 0)
					return a.getObjectID().compareTo(b.getObjectID());
				return e;
			}
			return d;
		}
		return c;
	}
	
}
