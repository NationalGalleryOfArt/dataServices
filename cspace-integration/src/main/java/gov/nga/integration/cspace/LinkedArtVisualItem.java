/*
    NGA ART DATA API: LinkedArtVisualItem extends the classified type and sets 
    its Type as "VisualItem" thus representing the Linked Art Entity "VisualItem"
    which typically has a format and possibly conformsTo a published specification
    standard such as the IIIF Image API

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// needs to support the following formats
// IIIF Images
//"representation": [
//                   {
//                     "id": "http://iiif.example.org/image/1", 
//                     "type": "VisualItem", 
//                     "label": "IIIF Image API for Sculpture", 
//                     "conforms_to": {"id": "http://iiif.io/api/image"}
//                   }
//                 ]
//                 // along with primary image or preferred depiction or something like that
//
//
// NON-IIIF Images


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.common.entities.art.Derivative.IMGFORMAT;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedArtVisualItem extends LinkedArtRecord {
	
	private List<LinkedArtBaseClass> conforms_to = CollectionUtils.newArrayList();

	public LinkedArtVisualItem() {
		super("VisualItem");
	}

	public LinkedArtVisualItem(String id, String label, LinkedArtClassifiedType... classifiedTypes) {
		super("VisualItem", label);
		setId(id);
		if (classifiedTypes != null) {
			for ( LinkedArtClassifiedType c : classifiedTypes) {
				addClassifiedAs(c);
			}
		}
	}
	
	private static Pattern suffixPattern = Pattern.compile("\\.\\w*$");
	public String getFormat() {
		String ext = null;
		String id = getId();
		Matcher m = suffixPattern.matcher(id);
		if ( m.find() ) {
			ext = m.group(0);
			return IMGFORMAT.formatFromExtension(ext).getMimetype();
		}
		else 
			return null;
	}

	public void addConforms_to(LinkedArtBaseClass l) {
		this.conforms_to.add(l);
	}
	
	public List<LinkedArtBaseClass> getConforms_to() {
		if (conforms_to!= null && conforms_to.size() > 0)
			return conforms_to;
		return null;
	}
	

}

