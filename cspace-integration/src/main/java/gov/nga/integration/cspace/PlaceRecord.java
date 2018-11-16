/*
    NGA ART DATA API: PlaceRecord is the JSON bean representing a Linked Art Place.

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

import gov.nga.entities.art.Place;
import gov.nga.utils.CollectionUtils;

@JsonPropertyOrder( { "namespace", "source", "id", "type", "label", "identified_by", "placeType", 
					  "isPublicEventsVenue", "describesTMSLocationIDs", "maps" 
				  })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceRecord extends LinkedArtRecord {
	
	// mandatory fields of the API - source and id are also mandatory and are inherited from the base object Record
    private Place place;
    private List<MapInfoRecord> mapinfo = null;
    
	public PlaceRecord(Place pl)  {
		super("Place");
		if ( pl == null)
			return;
		// TODO - for each type of private entity, we need a way to detect within the
		// entity itself whether the entity is public or private and return an ID accordingly
		// locations / places are one example
		setId("https://api.nga.gov/places/"+pl.getPlaceKey());
		setPlace(pl);
		addMapInfo(new MapInfoRecord(pl));
		
		addIdentity(
			new LinkedArtIdentifier(pl.getPlaceKey(),
				new LinkedArtClassifiedType("http://vocab.getty.edu/aat/300404012", "placeKey")
			)
		);
	}
	
	private void addMapInfo(MapInfoRecord mapinfo) {
		
		/* - should add this representation to the place record when available
		"identified_by": [
		                  {
		                    "id": "https://linked.art/example/Geometry/0", 
		                    "type": "Geometry", 
		                    "value": "POLYGON((165.74 -33.55, -179.96 -33.55, -179.96 -47.8, 165.74 -47.8, 165.74 -33.55))", 
		                    "conforms_to": {
		                      "id": "https://linked.art/example/info/1", 
		                      "type": "InformationObject", 
		                      "label": "ISO/IEC 13249-3:2016"
		                    }
		                  }
		                ], 
		*/
		
		if ( this.mapinfo == null)
			this.mapinfo = CollectionUtils.newArrayList();
		this.mapinfo.add(mapinfo);
	}
	
	private void setPlace(Place place) {
		this.place = place;
	}
	
	public List<MapInfoRecord> getMaps() {
		return this.mapinfo;
	}

	public String getLabel() {
		return place != null ? place.getDescription() : null;
	}

	public String getPlaceType() {
		return place != null ? place.getPlaceType() : null;
	}
	
	public Boolean getIsPublicEventsVenue() {
		return place != null ? place.isPublicEventsVenue() : false;
	}
	
	public List<Long> getDescribesTMSLocationIDs() {
		return place != null ? place.getTMSLocationIDs() : null;
	}

}

 