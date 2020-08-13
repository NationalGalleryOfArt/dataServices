/*
    Sortable indicates that a class can be sorted by a class-defined of sorting mechanisms
  
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

public interface Sortable {
	// matchesAspect is used for comparisons of two entities (a & b) given
	// with respect to a third entity (ao) rather than direct
	// comparisons between a & b themselves
    public Long matchesAspect(Object ao, Object order);
    
    // used for sorting two objects with respect to some ordering criteria
	public int aspectScore(Object ao, Object order, String matchString);
	
	// the default (preferred) sort order
	public SortOrder getDefaultSortOrder();
	
	// a natural sort order for the entity (preferably fast)
	public SortOrder getNaturalSortOrder();
}
