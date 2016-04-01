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