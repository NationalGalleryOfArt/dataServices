/*
    NGA ART DATA API: Reference is a loosely used predicate that is part of the ConservationSpace API to
    encapsulate but not define, per se, the relationships between two entities.  Instead, there's also a 
    predicate property inside of each reference that is more equivalent to the predicates found in Linked Art.
    In the future, this should be retired in favor of the Linked Art model.
  
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
package gov.nga.integration.cspace;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reference {
	private String[] predicates;
	private String predicate;
	private Record object;
	
	public Reference(String[] predicate, Record object) {
		setPredicates(predicate);
		setObject(object);
	}
	
	public String[] getPredicates() {
		return predicates;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicates(String[] predicate) {
		// skip setting predicates for now to ensure backwards compatibility
		//this.predicates = predicate;
		
		// for backwards compatibility, also set the "predicate" value
		// if the predicate namespace starts with "cspace:" and remove
		// the predicate namespace
		if (predicate != null) {
			this.predicate = predicate[0].replaceAll("cspace:", ""); 
			/* for (String p : predicate) {
				if ( p.startsWith("cspace:") ) {
					this.predicate = p.replaceAll("cspace:", ""); 
				}
			}
			*/
		}
	}
	public Record getObject() {
		return object;
	}
	public void setObject(Record object) {
		this.object = object;
	}
}
