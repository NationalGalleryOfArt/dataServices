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

import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Location extends ArtEntityImpl {
	
	protected static final String fetchAllLocationsQuery = 
		"SELECT l.fingerprint, l.locationID, l.site, " +
		"       l.room, l.publicAccess, l.description, " + 
		"       l.unitPosition " + 
		"FROM data.locations l";
	
	protected static final String briefLocationsQuery = 
		fetchAllLocationsQuery + " WHERE locationID @@ ";
	
	protected Location(ArtDataManagerService manager) {
		super(manager);
	}
	
	public Location(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,    TypeUtils.getLong(rs, 1));
		locationID 		= TypeUtils.getLong(rs, 2);
		site 			= rs.getString(3);
		room 			= rs.getString(4);
		publicAccess 	= TypeUtils.getLong(rs, 5);
		description 	= rs.getString(6);
		unitPosition 	= rs.getString(7);
	}
	
	public Place getPlace() {
		return this.getManager().fetchByTMSLocationID(getLocationID());
	}
	
	public Location factory(ResultSet rs) throws SQLException {
		Location at = new Location(getManager(),rs);
		return at;
	}
	
	private Long locationID = null;
	public Long getLocationID() {
		return locationID;
	}

	private String site = null;
	public String getSite() {
		return site;
	}

	private String room = null;
	public String getRoom() {
		return room;
	}

	private String description = null;
	public String getDescription() {
		return description;
	}

	private String unitPosition = null;
	public String getUnitPosition() {
		return unitPosition;
	}

	public Boolean isPublicLocation() {
		return TypeUtils.longToBoolean(getPublicAccess());
	}
	
	private Long publicAccess = null;
	private Long getPublicAccess() {
		return publicAccess;
	}
	
}