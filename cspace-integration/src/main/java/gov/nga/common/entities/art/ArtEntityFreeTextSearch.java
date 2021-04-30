/*
    NGA Art Data API: Art Entity Interface for free text search implementations 

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
package gov.nga.common.entities.art;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.search.Faceted;
import gov.nga.common.search.FreeTextSearchable;
import gov.nga.common.search.Searchable;
import gov.nga.common.search.Sortable;


public class ArtEntityFreeTextSearch<T extends ArtEntity & Faceted & Searchable & Sortable> implements FreeTextSearchable<T>{
	
	private static final Logger log = LoggerFactory.getLogger(ArtEntityFreeTextSearch.class);
	
	
	public List<T> freeTextSearch(List<Object> fields, String searchTerm, List<T> baseList) {
		log.error("*********** FREE TEXT SEARCH NOT IMPLEMENTED ***************");
		return baseList;
	}


	@Override
	public List<T> freeTextSearch(List<Enum<?>> fields, List<String> searchTerms, List<T> baseList) {
		log.error("*********** FREE TEXT SEARCH NOT IMPLEMENTED ***************");
		return baseList;
	}
		

}
