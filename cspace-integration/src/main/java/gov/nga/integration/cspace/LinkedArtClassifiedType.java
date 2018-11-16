/*
    NGA ART DATA API: LinkedArtClassifiedType extends the base class to also provide a list
    of elements used to classify the entity 

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
	"namespace", "source", "context", "id", "type", "label", "value", "classified_as"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtClassifiedType extends LinkedArtBaseClass {

	private List<LinkedArtBaseClass> classifiedAs = CollectionUtils.newArrayList();
	
	public LinkedArtClassifiedType(String type, String label, String value, LinkedArtClassifiedType... classifiedAs)  {
		super(type);
		setClassifiedAs(classifiedAs);
		setLabel(label);
		setValue(value);
	}

	// for types that don't need a label since they are exactly described by their classifications only
	public LinkedArtClassifiedType(String type, String value, LinkedArtClassifiedType... classifications)  {
		this(type,null,value,classifications);
	}

	private void setClassifiedAs(LinkedArtClassifiedType[] classifiedAs) {
		if (classifiedAs == null)
			return;
		for (LinkedArtClassifiedType c : classifiedAs)
			addClassifiedAs(c);
	}
	public void addClassifiedAs(LinkedArtBaseClass c) {
		classifiedAs.add(c);
	}
	public List<LinkedArtBaseClass> getClassified_as() {
		if ( classifiedAs != null && classifiedAs.size() > 0 )
			return classifiedAs;
		return null;
	}
	
}

