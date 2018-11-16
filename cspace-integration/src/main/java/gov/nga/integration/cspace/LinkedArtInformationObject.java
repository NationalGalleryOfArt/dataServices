/*
    NGA ART DATA API: LinkedArtInformationObject extends LinkedArtRecord and 
    represents the Linked Art Entity with Type = "InformationObject"

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
	"namespace", "source", "context", "id", "type"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtInformationObject extends LinkedArtRecord {
	
	private List<LinkedArtBaseClass> about = CollectionUtils.newArrayList();

	public LinkedArtInformationObject() {
		super("InformationObject");
	}
	
	public void addAbout(LinkedArtBaseClass l) {
		this.about.add(l);
	}
	
	public List<LinkedArtBaseClass> getAbout() {
		if (about != null && about.size() > 0)
			return about;
		return null;
	}
	
	
//	public LinkedArtInformationObject(String label, String value) {
//		super("InformationObject", label, value);
//	}

}

