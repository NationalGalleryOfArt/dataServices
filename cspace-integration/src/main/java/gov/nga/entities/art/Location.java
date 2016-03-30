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