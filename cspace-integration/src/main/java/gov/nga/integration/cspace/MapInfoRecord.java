// TODO - consider renaming to something like LocationOnMap or something to that effect
/*
    NGA ART DATA API: MapInfoRecord is the JSON bean for representing a map that
    might have an image and geometrical shapes on that image, e.g. a 2 dimensionsal
    floor map of a building with spaces defined relative to the geometry of the image
    identifying a particular room on that map

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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.Place;
import gov.nga.entities.art.Place.Polygon;

@JsonPropertyOrder({ "mapType", "source", "mapImageURL", "mapShape" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapInfoRecord extends Record {

    private Place place;
    
	public MapInfoRecord(Place pl)  {
		if (pl == null)
			return;
		setPlace(pl);
		setSource(null);
	}
	
	public void setPlace(Place pl) {
		this.place = pl;
	}

	public String getMapType() {
		return "2DFloorMap";
	}
	
	//public String getMapImagePath() {
	//	return place.getMapImagePath();
	//}
	
	public String getMapImageURL() {
		return place.getMapImageURL();
	}

	public Polygon getMapShape() {
		return place.getMapShape();
	}

}

 