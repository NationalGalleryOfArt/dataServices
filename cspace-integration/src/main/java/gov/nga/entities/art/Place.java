/*
    NGA Art Data API: Location is a class that represents a place where art
    is displayed.  It is not necessarily trying to represent the same concept as
    the place a person was born or an art object created although I can imagine
    it evolving in that direction as we integrate controlled vocabularies more
    thoroughly in our data sets.   

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
package gov.nga.entities.art;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Place extends ArtEntityImpl {
	
	protected static final String fetchAllPlacesQuery = 
			"SELECT pl.locationKey, pl.locationType, pl.description, pl.isPublicVenue, " +
			"       pl.mapImageURL, pl.mapImageURLPath, pl.mapShapeType, pl.mapShapeCoords " + 
			"FROM data.preferred_locations pl";
	
	protected Place(ArtDataManagerService manager) {
		super(manager);
	}
	
	public Place(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager);
		placeKey		= rs.getString(1);
		placeType	= rs.getString(2);
		description		= rs.getString(3);
		isPublicEventsVenue 	= TypeUtils.getLong(rs, 4);
		mapImageURL		= rs.getString(5);
		mapImagePath	= rs.getString(6);
		mapShapeType	= rs.getString(7);
		mapShapeCoords	= rs.getString(8);
		tmsLocationIDs = CollectionUtils.newArrayList();
	}
	
	public Place factory(ResultSet rs) throws SQLException {
		Place at = new Place(getManager(),rs);
		return at;
	}
	
	private List<Long> tmsLocationIDs = null;
	protected void addTMSLocationID(Long id) {
		tmsLocationIDs.add(id);
	}
	
	public List<Long> getTMSLocationIDs() {
		return CollectionUtils.newArrayList(tmsLocationIDs);
	}
	
	private String placeKey = null;
	public String getPlaceKey() {
		return placeKey;
	}

	private String placeType = null;
	public String getPlaceType() {
		return placeType;
	}

	private String description = null;
	public String getDescription() {
		return description;
	}

	private String mapImagePath = null;
	public String getMapImagePath() {
		return mapImagePath;
	}

	private String mapImageURL = null;
	public String getMapImageURL() {
		return mapImageURL;
	}

	private String mapShapeType = null;

	public class BadGeometryException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public class Polygon {
		List<Long[]> coordinates = CollectionUtils.newArrayList();
		void addCoordinate(long x, long y) {
			Long[] newCoord = new Long[] {x,y};
			coordinates.add(newCoord);
		}
		
		public List<Long[]> getCoordinates() {
			return coordinates;
		}
		
		public String getType() {
			return "polygon";
		}
		
		void validate() throws BadGeometryException {
			if ( coordinates.size() < 2 )
				throw new BadGeometryException();
		}
		
		Polygon() {
		}
		
		Polygon(Long[] data) throws BadGeometryException {
			try {
				// assign the data in pairs of two to new coordinates
				for ( int j=0; j<data.length; j=j+2 ) {
					addCoordinate(data[j],data[j+1]);
				}
				validate();
			}
			catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
				throw new BadGeometryException();
			}
		}
	}
	
	public class Rectangle extends Polygon {
		@Override
		void validate() throws BadGeometryException {
			if ( coordinates.size() != 2 )
				throw new BadGeometryException();
		}
		
		Rectangle(Long[] data) throws BadGeometryException {
			super(data);
		}

		@Override
		public String getType() {
			return "rectangle";
		}

	}
	
	public class Circle extends Polygon {
		Long radius;
		private void setRadius(Long radius) {
			this.radius = radius;
		}
		public Long getRadius() {
			return radius;
		}
		
		@Override
		void validate() throws BadGeometryException {
			if ( coordinates.size() != 1 || radius == null || radius == 0)
				throw new BadGeometryException();
		}
		
		Circle(Long[] data) throws BadGeometryException {
			try {
				// assign the data in pairs of two to new coordinates
				addCoordinate(data[0], data[1]);
				setRadius(data[2]);
				validate();
			}
			catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
				throw new BadGeometryException();
			}
		}
		
		public String getType() {
			return "circle";
		}

	}
	

	private String mapShapeCoords = null;
	public Polygon getMapShape() {
		if ( StringUtils.isNullOrEmpty(mapShapeType) || StringUtils.isNullOrEmpty(mapShapeType))
			return null;
		try {

			List<Long> data = CollectionUtils.newArrayList(); 
			for ( String l : mapShapeCoords.split(",") ) {
				data.add(Long.parseLong(l));
			}
			
			if ( mapShapeType.equals("rect") )
				return new Rectangle(data.toArray(new Long[0]));
			else if ( mapShapeType.equals("poly") )
				return new Polygon(data.toArray(new Long[0]));
			else if ( mapShapeType.equals("circle") )
				return new Circle(data.toArray(new Long[0]));
		}
		catch ( BadGeometryException | NumberFormatException be) {
		}
		return null;
	}

	public Boolean isPublicEventsVenue() {
		return TypeUtils.longToBoolean(getIsPublicEventsVenue());
	}
	
	private Long isPublicEventsVenue = null;
	private Long getIsPublicEventsVenue() {
		return isPublicEventsVenue;
	}
	
}