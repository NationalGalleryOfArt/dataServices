/*
    NGA ART DATA API: ProductionRecord provides the JSON bean of the Production Linked Art entity
  
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

import gov.nga.entities.art.ArtObjectConstituent;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { 	
	"namespace", "source", "context", "id", "type", "carried_out_by"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductionRecord extends LinkedArtBaseClass {

	private static final String defaultNamespace = "Production";

	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
	private AbridgedObjectRecord artObjectRecord;

	public ProductionRecord(AbridgedObjectRecord artObjectRecord, String predicate)  {
		super(defaultNamespace);
		if (artObjectRecord != null) {
			setArtObjectRecord(artObjectRecord);
			setId(artObjectRecord.getUrl().toString().replaceAll("\\.json", "") + "/" + predicate);
		}
	}
	
	private void setArtObjectRecord(AbridgedObjectRecord artObjectRecord) {
		this.artObjectRecord = artObjectRecord;
	}

	public List<ActorRecord> getCarried_out_by() {
		List<ActorRecord> l = CollectionUtils.newArrayList();
		for ( ArtObjectConstituent oc : artObjectRecord.getArtObject().getArtists() ) {
			l.add(new ActorRecord(oc.getConstituent()));
		}
		return l;
	}
	
}

