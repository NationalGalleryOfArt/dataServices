/*
    SearchFilter provides an implementation for filtering (primarily string based) data in a 
    variety of configurable modes
  
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

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import gov.nga.search.SearchHelper.SEARCHOP;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;
import gov.nga.utils.hashcode.CustomHash;

public class SearchFilter implements CustomHash {

//	private static final Logger log = LoggerFactory.getLogger(SearchFilter.class);
	
	SEARCHOP op = null;
	Object field = null;
	private List<String> strings = null;
	private List<String> stringsNormalized = null;
	private List<Long> longs = null;
    private Boolean normalize = false;
    
    public long customHash() {
    	HashCodeBuilder hcb = new HashCodeBuilder(13,17);
    	hcb.append(normalize);
    	if (longs != null) {
    		for (Long l : longs)
    			hcb.append(l);
    	}
    	if (stringsNormalized != null) {
    		for (String s : stringsNormalized)
    			hcb.append(s);
    	}
    	if (strings != null) {
    		for (String s : strings)
    			hcb.append(s);
    	}
    	hcb.append(field);
    	hcb.append(op);
    	return hcb.hashCode();
    }

	private SearchFilter(SEARCHOP o, Object s) {
		op = o;
		field = s;
	} 

    public SearchFilter(SEARCHOP o, Object s, List<String> strings) {
    	this(o,s);
    	for (String st : strings) {
    		addValue(st);
    	}
    }

    public SearchFilter(SEARCHOP o, Object s, List<String> strings, Boolean doNormalize) {
    	this(o,s,strings);
    	setNormalize(doNormalize);
    }
    
    private void addValue(String val) {
    	if (strings == null)
    		strings = CollectionUtils.newArrayList();
   		if (stringsNormalized == null)
   			stringsNormalized = CollectionUtils.newArrayList();
   		if (longs == null)
   			longs = CollectionUtils.newArrayList();
   		
   		if (val != null) {
   			strings.add(val.toLowerCase());
        	stringsNormalized.add(StringUtils.removeDiacritics(val).toLowerCase());
			// try to decode a whole number out of the string to support implicit numerical matches 
			try {
				Long l = Long.decode(val);
				longs.add(l);
			}
			catch (NumberFormatException nfe) {
				// not a number, so append a null value to the list so we keep all of our lists
				// in sync
				longs.add(null);
			}
   		}
   		else { 
        	stringsNormalized.add(null);
        	strings.add(null);
        	longs.add(null);
       	}
    }
    
	public SearchFilter(SEARCHOP o, Object s, String v1) {
		this(o,s);
		addValue(v1);
        //log.info("SearchFilter looking for: " + v1);
	}

	public SearchFilter(SEARCHOP o, Object s, String v1, String v2) {
		this(o,s,v1);
		addValue(v2);
	}
    
	public SearchFilter(SEARCHOP o, Object s, String v1, Boolean doNormalize) {
		this(o,s,v1);
        setNormalize(doNormalize);
	}
    
	public SearchFilter(SEARCHOP o, Object s, String v1, String v2, Boolean doNormalize) {
		this(o,s,v1,v2);
        setNormalize(doNormalize);
	}
    
	private void setNormalize(Boolean b) {
		normalize = b;
	}
    
	private Boolean getNormalize() {
		return normalize;
	}
    
	public Object getField() {
		return field;
	}
	
	private List<String> getStrings() {
		return getNormalize() ? getStringsNormalized() : strings;
	}
    
	private List<String> getStringsNormalized() {
		return stringsNormalized;
	}
	
	public SEARCHOP getOp() {
		return op;
	}
	
	private Long stringToLong(String string) {
		Long myLong = null;
		if (string == null)
			return null;

		try {
			myLong = Long.decode(string);
		}
		catch (NumberFormatException nfe) { 
		}
		return myLong;
	}
	
	public List<String> getStringSearchValues() {
		return CollectionUtils.newArrayList(strings);
	}
	
	public List<Long> getLongSearchValues() {
		return CollectionUtils.newArrayList(longs);
	}

	private boolean lessthanOrEqualTo(Long longLeft, String stringLeft,
							 Long longRight, String stringRight) {

		// if longs are defined for both values, then compare using longs
		if (longLeft != null && longRight != null)
			return longLeft <= longRight;
		
		// otherwise, compare strings
		else {

			if (stringLeft == null && stringRight == null)
				return true;
			else if (stringLeft == null || stringRight == null)
				return false;
			else {
				// might want to consider using the collator compare
				// for completeness sake - otherwise strange results
				// might happen when comparing strings with diacritics
				// StringUtils.getDefaultCollator().compare(a, b);
				return stringLeft.compareTo(stringRight) <= 0; 
			}
		}
	}
	
	public Boolean filterMatch(String sourceStringLow, String sourceStringHigh) {
	
		// special handling for INTERSECTS operator otherwise
		// multiple values passed to filter match are determined by
		// OR'ing the two matches together.
		if (getOp() != SEARCHOP.INTERSECTS)
			return filterMatch(sourceStringLow) || filterMatch(sourceStringHigh);

		// THE FOLLOWING EXCLUSIVELY DEALS WITH INTERSECTIONS OF A GIVEN SET OF START AND 
		// END POINTS WITH VALUE PAIRS THAT MAY BE 
		// NUMERICAL OR STRING AND COMPARISON OF THOSE SEARCH CRITERIA WITH THE
		// SUPPLIED SOURCE STRINGS WHICH COME FROM FULL POPULATION OF A DATA SET SUCH
		// AS ART OBJECTS, CONSTITUENTS, ETC... ANY OF THE VALUES MAY BE NULL
		
		// pseudo-code algorithm for handling intersections is as follows with
		// notable logical exception granted for condition where data value pairs are both null
		// in which case, rather than treating the data as an open ended line encompassing all possible
		// values, we instead treat it as completely undefined and therefore do not return a match
		// if the source pairs are both null, that's treated as open ended and all data pairs match
		// in such cases
		
		/* PSEUDOCODE FOR EACH COMPARISON
		both are dual open end	true (redundant, no need to check due to next condition)
		else 1 is dual open end		true - since encompasses all possible values - doesn't 
		else 2 is dual open end		false (except when 1 is dual open end)
		else if 1b is null		2b is null OR 2b <= 1e
		else if 1e is null		2e is null OR 2e >= 1b
		else if 2b is null (and 1 is def)	2e >= 1b
		else if 2e is null (and 1 is def) 	2b <= 1e
		*/
		
		// source strings are data from the population of potential entities 
		// search strings are the search criteria pairs provided by the caller 
		
		// convert source string to the proper format based on whether we're normalizing diacritics or not
		if (sourceStringLow != null)
			sourceStringLow = getNormalize() ? StringUtils.removeDiacritics(sourceStringHigh).toLowerCase() : sourceStringLow.toLowerCase();
		if (sourceStringHigh != null)
			sourceStringHigh = getNormalize() ? StringUtils.removeDiacritics(sourceStringHigh).toLowerCase() : sourceStringHigh.toLowerCase();
		Long sourceLongLow = stringToLong(sourceStringLow);
		Long sourceLongHigh = stringToLong(sourceStringHigh);

		// now, an intersection is measured by comparing the max of the starting points 
		// in a range and making sure that it's less than the MIN of the ending points
		// thus, we need four values to continue
		
		// if we have insufficient data to use for comparisons, then we return false;
		List<String> list = getStrings();
		if (list == null || list.size() < 2)
			return false;
		
		// otherwise, we traverse the list comparing our source points with the given ranges
		for (int j=0; j<list.size(); j=j+2) {
			String searchStringLow  = list.get(j);
			String searchStringHigh = list.get(j+1);

			// if segment 1 is dual open end, then true 
			if (searchStringLow==null && searchStringHigh==null)
				return true;
			
			// 	else if segment 2 is dual open end		false (except when 1 is dual open end)
			else if (sourceStringLow==null && sourceStringHigh==null)
				continue;	// skip to the next search string in our list

			// we have some data, so proceed with type conversions as needed
			else {
				Long searchLongLow = longs.get(j);
				Long searchLongHigh = longs.get(j+1);

				// now, trim the source for the purpose of string comparisons only
				// otherwise, string comparisons fail because there could be more text
				// to compare in the source than we really want to look at
				String sourceStringTrimmedLow  = StringUtils.trimToMatchSize(sourceStringLow,searchStringLow);
				String sourceStringTrimmedHigh = StringUtils.trimToMatchSize(sourceStringHigh,searchStringHigh);

				// else if segment one start is null		return segment 2 start is null OR 2 start <= 1 end
				if (searchStringLow == null) {
					if (sourceStringLow == null || lessthanOrEqualTo(sourceLongLow,sourceStringTrimmedLow,searchLongHigh,searchStringHigh))
						return true;
				}
				
				//	else if segment one end is null		segment 2 end is null OR segment 2 end >= segment one start
				else if (searchStringHigh == null) {
					if (sourceStringHigh == null || lessthanOrEqualTo(sourceLongHigh, sourceStringTrimmedHigh, searchLongLow, searchStringLow))
						return true;
				}
				
				// else if 2b is null (and 1 is def)	2e >= 1b
				else if (sourceStringLow == null) {
					if (lessthanOrEqualTo(sourceLongHigh, sourceStringTrimmedHigh, searchLongLow, searchStringLow))
						return true;
				}
				
				// 	else if 2e is null (and 1 is def) 	2b <= 1e
				else if (sourceStringHigh == null) {
					if (lessthanOrEqualTo(sourceLongLow, sourceStringTrimmedLow, searchLongHigh, searchStringHigh))
						return true;
				}
				
				// else return normal case which is if max of begin points is less than the min of end points
				// we have data for all points and none of them is null - some could be longs
				else {
					if (sourceLongLow != null && sourceLongHigh != null && searchLongLow != null && searchLongHigh != null)
						// calculate intersection using Long comparisons - intersects if max of lows
						// is less than or equal to the min of highs
						if (Math.max(searchLongLow, sourceLongLow) <= Math.min(searchLongHigh,sourceLongHigh))
							return true;
					else {
						// calculate the max low and min high on a string basis
						String maxLow =	( lessthanOrEqualTo(null, sourceStringLow, null, searchStringLow) ) ? searchStringLow : sourceStringLow;
						String minHigh = ( lessthanOrEqualTo(null, sourceStringHigh, null, searchStringHigh) ) ? sourceStringHigh : searchStringHigh;
						// as for numbers, there is an intersection when max of lows is <= min of highs
						if (lessthanOrEqualTo(null,maxLow,null,minHigh))
							return true;
					}
				}
			}
		}

		// by default, filter passes nothing since we're AND'ing filters together
		return false;
	}
    
	public Boolean filterMatch(String source) {
		//log.error("op is: " + getOp() + "; string1 is: " + getString1() + "; source is: " + source);
		
		// convert source string to the proper format based on whether we're normalizing diacritics or not
		if (source != null)
			source = getNormalize() ? StringUtils.removeDiacritics(source).toLowerCase() : source.toLowerCase();

		List<String> list = getStrings();
		
		// if we have no data to use for comparisons with the source, then we return false;
		if (list == null || list.size() < 1)
			return false;
		
		switch (getOp()) {

		case EQUALS:
		case IN:
			// if any value supplied in the list matches the given source then return true
			// the EQUALS operator is the SAME as IN but provided for code clarity in calling programs 
			for (String s : list) {
				if	( ( source == null && s == null ) || 		// either both source and supplied are both null
					  ( source != null && source.equals(s) ) )  // or source is not null and also equals s
					  return true;
			}
			break;
		case STARTSWITH:
			// if the source value starts with any of the values in the given list then return true
			for (String s : list) {
				if	( ( source == null && s == null ) || 			// either both source and supplied are both null
					  ( source != null && source.startsWith(s) ) )  // or source is not null and starts with s
					  return true;
			}
			break;
		case LIKE:
			// if the source value starts with any of the values in the given list then return true
			for (String s : list) {
				if	( ( source == null && s == null ) || 					   // either both source and supplied are both null
					  ( source != null && s != null && source.contains(s) ) )  // or source and s are not null and source contains s
					  return true;
			}
			break;
		case BETWEEN:
			// between is a special case in that the supplied values are taken in pairs
			// if the source value is between with any of the sets of values in the given list then return true
			// we iterate the list differently because each supplied value might also be decoded as a number
			// and if all available values are numbers, then we prefer that comparison method to string comparisons
			for (int j=0; j<list.size(); j=j+2) {
				String stringLow  = list.get(j);
				String stringHigh = list.get(j+1);
				
				// handle special case of all values == null
				if (stringLow == null && stringHigh == null && source == null)
					return true;

				// otherwise proceed to see whether we should compare based on numerals or strings
				Long longLow = longs.get(j);
				Long longHigh = longs.get(j+1);
				Long sourceLong = null;
				if (source != null) {
					try {
						sourceLong = Long.decode(source);
					}
					catch (NumberFormatException nfe) { 
						// ignore 
					}
				}

				// by default, the filter on the low side passes only if the source
				// is greater than or equal to the low side of the filter or the 
				// low side of the filter is null which we take to mean no limit
				boolean passesLow = false;
				if (longLow != null && sourceLong != null) {
					passesLow = sourceLong.compareTo(longLow) >= 0;
				}
				else {
					// now, trim the source for the purpose of string comparisons only
					// otherwise, string comparisons fail because there could be more text
					// to compare in the source than we really want to look at
					String sourceLow  = StringUtils.trimToMatchSize(source,stringLow);
					if (stringLow != null && sourceLow != null) {
						passesLow = sourceLow.compareTo(stringLow) >= 0;
					}
					else if (stringLow == null) {
						passesLow = true;
					}
				}

				boolean passesHigh = false;
				if (longHigh != null && sourceLong != null) {
					passesHigh = sourceLong.compareTo(longHigh) <= 0;
				}
				else {
					// now, trim the source for the purpose of string comparisons only
					String sourceHigh = StringUtils.trimToMatchSize(source,stringHigh);
					if (stringHigh != null && sourceHigh != null) {
						passesHigh = sourceHigh.compareTo(stringHigh) <= 0;
					}
					else if (stringHigh == null) {
						passesHigh = true;
					}
				}

				// return true in the event that the source is in fact between at least one
				// pair of values supplied via the list
				if (passesLow && passesHigh)
					return true;
			}
			break;
		case INTERSECTS:
			throw new NotImplementedException("INTERSECTS currently not implemented for single value");
		}

		// by default, filter passes nothing since we're AND'ing filters together
		return false;
	}

}


