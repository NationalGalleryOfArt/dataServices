package gov.nga.entities.art;

import gov.nga.entities.art.ArtObject;
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
