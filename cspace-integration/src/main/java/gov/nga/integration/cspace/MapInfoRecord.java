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

 