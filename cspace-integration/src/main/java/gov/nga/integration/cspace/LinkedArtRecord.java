/*
    NGA ART DATA API: LinkedArtRecord extends the classified type and defines three lists
    of other linked art entities (referredToBy, identities, and representation) for capturing
    the types of relationships that can exist in some of the more complex linked art entities 

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


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "id", "type", "label", "source", "classified_as", "identified_by", "referred_to_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtRecord extends LinkedArtClassifiedType {

	private List<LinkedArtBaseClass> referredToBy = CollectionUtils.newArrayList();
	private List<LinkedArtBaseClass> identities = CollectionUtils.newArrayList();
	private List<LinkedArtBaseClass> representation = CollectionUtils.newArrayList();

	public LinkedArtRecord(String type)  {
		this(type, null, null);
	}

	public LinkedArtRecord(String type, String label)  {
		this(type, label, null);
	}

	public LinkedArtRecord(String type, String label, String value)  {
		super(type, label, value);
		referredToBy = CollectionUtils.newArrayList();
		identities = CollectionUtils.newArrayList();
	}


	protected void addReferredToBy(LinkedArtBaseClass o) {
		referredToBy.add(o);
	}
	public List<LinkedArtBaseClass> getReferred_to_by() {
		if (referredToBy != null && referredToBy.size() > 0)
			return referredToBy;
		return null;
	}

	protected void addIdentity(LinkedArtBaseClass o) {
		identities.add(o);
	}
	public List<LinkedArtBaseClass> getIdentified_by() {
		if (identities != null && identities.size() > 0)
			return identities;
		return null;
	}

	protected void addRepresentation(LinkedArtBaseClass o) {
		representation.add(o);
	}
	public List<LinkedArtBaseClass> getRepresentation() {
		if (representation != null && representation.size() > 0)
			return representation;
		return null;
	}

}

