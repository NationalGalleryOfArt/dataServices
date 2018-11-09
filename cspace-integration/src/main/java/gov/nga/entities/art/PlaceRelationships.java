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

public class PlaceRelationships extends ArtEntityImpl {
	
	protected static final String fetchAllPlaceTMSLocationsQuery = 
			"SELECT pltl.preferredLocationKey, pltl.tmsLocationID " +
			"FROM data.preferred_locations_tms_locations pltl";
	
	protected PlaceRelationships(ArtDataManagerService manager) {
		super(manager);
	}
	
	public PlaceRelationships(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		this(manager);
		preferredLocationKey 	= rs.getString(1);
		tmsLocationID			= TypeUtils.getLong(rs,  2);
	}
	
	public PlaceRelationships factory(ResultSet rs) throws SQLException {
		PlaceRelationships at = new PlaceRelationships(getManager(),rs);
		return at;
	}
	
	private String preferredLocationKey = null;
	public String getPlaceKey() {
		return preferredLocationKey;
	}

	private Long tmsLocationID = null;
	public Long getTMSLocationID() {
		return tmsLocationID;
	}
	
}