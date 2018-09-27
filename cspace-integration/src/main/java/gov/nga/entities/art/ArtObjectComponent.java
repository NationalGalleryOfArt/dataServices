/*
    NGA Art Data API: Art Entity for Components of an art object - each object
    can actually be comprised of multiple parts, e.g. base of a sculpture vs. the
    sculpture itself, or two parts that can be disassembled and stored in different
    locations, etc. 

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

public class ArtObjectComponent extends ArtEntityImpl {
	
	protected static final String fetchAllComponentsQuery = 
		"SELECT c.fingerprint, c.componentID, c.objectID, c.componentType, c.locationID, c.homeLocationID, " + 
				"c.componentName, c.componentNumber " +
		"FROM data.components c";
	
	protected ArtObjectComponent(ArtDataManagerService manager) {
		super(manager);
	}
	
	public ArtObjectComponent(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,    TypeUtils.getLong(rs, 1));
		componentID		= TypeUtils.getLong(rs, 2);
		objectID		= TypeUtils.getLong(rs, 3);
		componentType	= TypeUtils.getLong(rs, 4);
		locationID		= TypeUtils.getLong(rs, 5);
		homeLocationID	= TypeUtils.getLong(rs, 6);
		componentName	= rs.getString(7);
		componentNumber = rs.getString(8);
	}
	
	public ArtObjectComponent factory(ResultSet rs) throws SQLException {
		ArtObjectComponent at = new ArtObjectComponent(getManager(),rs);
		return at;
	}
	
	private Long locationID = null;
	public Long getLocationID() {
		return locationID;
	}

	private Long componentID = null;
	public Long getComponentID() {
		return componentID;
	}

	private Long homeLocationID = null;
	public Long getHomeLocationID() {
		return homeLocationID;
	}

	private Long objectID = null;
	public Long getObjectID() {
		return objectID;
	}
	
	private String componentName = null;
	public String getComponentName() {
		return componentName;
	}

	private String componentNumber = null;
	public String getComponentNumber() {
		return componentNumber;
	}

	private Long componentType = null;
	public Long getComponentType() {
		return componentType;
	}


}