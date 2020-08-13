/*
    SortHelper orchestrates the sorting of Sortable result sets 
  
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// class that provides utilities for sorting lists of objects
// instantiating a new object (rather than using static methods) ensures that 
// sets of sort options can be accessed by the Comparators without having to
// synchronize on them - since this is a small footprint class, this shouldn't present
// much of a problem.
public class SortHelper<E extends Sortable> extends Sorter {
	
//	private static final Logger log = LoggerFactory.getLogger(SortHelper.class);

	private Object baseEntity = null;
	private boolean removeBaseEntity = false;
	private Map<Object, String> matchStrings = CollectionUtils.newHashMap();
	public Comparator<E> sortByEntityAttributes = null;

	public long customHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(7,11);
		for (Object o : matchStrings.keySet())
			hcb.append(o.toString()).append(matchStrings.get(o));
		hcb.append(baseEntity);
		hcb.append(removeBaseEntity);
		hcb.append(super.customHash());
		return hcb.hashCode();
	}

	public void setBaseEntity(Object ao) {
		baseEntity = ao;
		
	}
	
	public void setRemoveBaseEntityFromSortedResults(boolean b) {
		removeBaseEntity = b;
	}

	public SortHelper(Object... order) {
		this(new SortOrder(order));
	}

	public SortHelper(SortOrder so) {
		super(so);
		this.sortByEntityAttributes = getComparator();
	}
	
	public SortHelper() {
		this((SortOrder) null);
	}

	// the whole notion here is to measure the closeness of two Art Objects
	// to a third art object (the base object) and to rank the ordering based 
	// on that score and if that score is the same, then we rank by comparing
	// just the two objects themselves
	// todo make private again
	public Comparator<E> getComparator() {
		return new Comparator<E>() {
			public int compare(E a, E b) {

				List<Object> so = getSortOrder();
				// first we see if there's a base object set and if there is, then
				// we compare the two given Art Objects against it and then sort based on score
				if (so != null) {
					for (Object w : so) {
						if (baseEntity != null) {
							// matchesAspect is used for comparisons of the two entities given
							// to us (a & b) with respect to a third entity rather than direct
							// comparisons between a & b.

							// matchesAspect should return a positive Long if a match with a base entity 
							// is found, 0 if one is not found or null if a comparison 
							// cannot be made on the given dimension
							Long aMatch = a.matchesAspect(baseEntity, w);
							Long bMatch = b.matchesAspect(baseEntity, w);
							Integer res = compareMatchesPreferLarger(aMatch, bMatch);

							// if one object matches more closely to the base object than the other for the 
							// given dimension, then we return a score for it, otherwise, we proceed to 
							// comparing the objects w.r.t. each other only
							if (res != null)
								return res;
						}

						// either the base entity is null or
						// the entities have nothing in common with the 
						// base entity or the given SORT comparison is not
						// applicable to comparisons with a base entity
						int match = a.aspectScore(b, w, matchStrings.get(w));
						if (match != Sorter.NULL)
							return match;
					}
				}

				// and finally, if the entities themselves are 
				// completely equivalent with respect to the 
				// given sort order or the sort cannot be computed 
				// for some reason, then we use the default sort 
				// order for the entity instead
				SortOrder naturalOrder = a.getNaturalSortOrder();
				if (naturalOrder != null && naturalOrder.getSortOrder() != null) {
					for (Object w : naturalOrder.getSortOrder()) {
						int score = a.aspectScore(b, w, matchStrings.get(w));
						if (score != Sorter.NULL)
							return score;
					}
				}

				// and lastly, if we cannot find any differences, then they are truly equivalent
				// as far as we are concerned, so we return zero
				return 0;
			}
		};
	}

	public void setMatchString(Object sortEnum, String matchString) {
		matchStrings.put(sortEnum, matchString);
	}
	
	public void sortArtEntities(List<E> entities, Object base, Object... order) {
		setSortOrder(order);
		setBaseEntity(base);
		sortEntities(entities);
	}
	
	public void sortArtEntities(List<E> entities, Object... order) {
		setSortOrder(order);
		sortEntities(entities);
	}
	
	public Set<E> createAutoSortedSet() {
		return CollectionUtils.newTreeSet(this.sortByEntityAttributes);
	}

	// TODO - refactor this entire class's interaction with SearchHelper to use a TreeSet rather than sorting
	// after all the results have been collected - just remember that treeset cannot have duplicate keys so we have to append
	// the objectid to the key - it should be fairly easy to do this although the base entity comparisons might be a little more
	// difficult so we might just have to scrap all that - we'll see... for now, the cspace integration probably performs well enough 
	public void sortEntities(List<E> artEntities) {
		if (removeBaseEntity && baseEntity != null && artEntities.contains(baseEntity))
			artEntities.remove(baseEntity);
		Collections.sort(artEntities, sortByEntityAttributes);
	}

/*	public static List<ArtObjectConstituent> sortObjectRoles(List<ArtObjectConstituent> objectRoles) {
		Collections.sort(objectRoles,ArtObjectConstituent.BYDISPLAYORDER);
		return objectRoles;
	}
*/
}
