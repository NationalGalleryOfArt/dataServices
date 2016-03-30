package gov.nga.search;
 
import gov.nga.search.SortOrder;
import gov.nga.search.Sortable;
import gov.nga.search.Sorter;
import gov.nga.utils.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
	
	public SortHelper(SortOrder so) {
		super(so);
	}
	
	public SortHelper() {
		super();
	}
	
	// the whole notion here is to measure the closeness of two Art Objects
	// to a third art object (the base object) and to rank the ordering based 
	// on that score and if that score is the same, then we rank by comparing
	// just the two objects themselves
	protected Comparator<E> sortByEntityAttributesOld = new Comparator<E>() {
		public int compare(E a, E b) {
			
			List<Object> so = getSortOrder();
			// first we see if there's a base object set and if there is, then
			// we compare the two given Art Objects against it and then sort based on score
			if (baseEntity != null && so != null) {
				for (Object w : so) {
					// matchesAspect returns a positive Long if a match is found, 0 if one is not found
					// or null if a comparison cannot be made on the given dimension
					Long aMatch = a.matchesAspect(baseEntity, w);
					Long bMatch = b.matchesAspect(baseEntity, w);
					Integer res = compareMatchesPreferLarger(aMatch, bMatch);
				//	log.error("checking aspect " + w);
				//	log.error("match between " + ((ArtObject) a).getObjectID() + " :" + ((ArtObject) baseEntity).getObjectID() + " :" + aMatch);
				//	log.error("match between " + ((ArtObject) b).getObjectID() + " :" + ((ArtObject) baseEntity).getObjectID() + " :" + bMatch);
				//	log.error("resulting in sort value of:" + res);

					// if one object matches more closely to the base object than the other for the given dimension,
					// then we return a score for it, otherwise, we proceed to comparing the objects
					// w.r.t. each other only
					if (res != null)
						return res;
				}
			}
			
			if (so != null) {
				// if we get here then we have some entities have nothing in common with the 
				// base entity or are equivalent in terms of the base entity so we have to 
				// sort them with each other instead.
				for (Object w : so) {
					//log.error("checking aspect " + w);
					Integer match = a.aspectScore(b, w, matchStrings.get(w));
					//log.error("match between " + ((ArtObject) a).getObjectID() + " :" + ((ArtObject) b).getObjectID() + " :" + match);
					if (match != null)
						return match;
				}
			}

			// and finally, if the entities themselves are equivalent with respect to the 
			// given sort order, then we use the default sort order for the entity instead
			SortOrder naturalOrder = a.getNaturalSortOrder();
			if (naturalOrder != null && naturalOrder.getSortOrder() != null) {
				for (Object w : naturalOrder.getSortOrder()) {
					//	log.error("checking aspect " + w);
					Integer score = a.aspectScore(b, w, matchStrings.get(w));
					//	log.error("score between " + ((ArtObject) a).getObjectID() + " :" + ((ArtObject) b).getObjectID() + " :" + score);
					if (score != null)
						return score;
				}
			}
			
			// and lastly, if we cannot find any differences, then they are truly equivalent
			// log.error("no score difference between " + ((ArtObject) a).getObjectID() + " :" + ((ArtObject) b).getObjectID());
			return 0;
		}
	};
	
	// the whole notion here is to measure the closeness of two Art Objects
	// to a third art object (the base object) and to rank the ordering based 
	// on that score and if that score is the same, then we rank by comparing
	// just the two objects themselves
	public Comparator<E> sortByEntityAttributes = new Comparator<E>() {
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
					Integer match = a.aspectScore(b, w, matchStrings.get(w));
					if (match != null)
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
					Integer score = a.aspectScore(b, w, matchStrings.get(w));
					if (score != null)
						return score;
				}
			}
			
			// and lastly, if we cannot find any differences, then they are truly equivalent
			// as far as we are concerned, so we return zero
			return 0;
		}
	};

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
