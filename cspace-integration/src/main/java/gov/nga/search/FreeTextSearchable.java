/*
    FreeTextSearchable indicates than a class' in-memory cached properties can be free-text searched
  
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

// public interface FreeTextSearchable<T extends FreeTextSearchable<T>> {
public interface FreeTextSearchable<T extends Faceted & Searchable & Sortable> {
	public List<T> freeTextSearch(List<Object> fields, String searchTerm, List<T> baseList);
}
