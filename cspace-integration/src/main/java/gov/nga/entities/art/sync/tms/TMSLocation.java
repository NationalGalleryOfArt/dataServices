package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.common.entities.art.Location;
import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.entities.art.ArtEntity;
import gov.nga.entities.art.Place;
import gov.nga.entities.common.FingerprintedEntity;
import gov.nga.utils.db.DataSourceService;
import gov.nga.utils.stringfilter.EmptyFilter;
import gov.nga.utils.stringfilter.StringFilter;

public class TMSLocation extends Location implements ArtEntity
{
	public static final String fetchAllLocationsQuery = 
			"SELECT l.fingerprint, l.locationID, l.site, " +
			"       l.room, l.publicAccess, l.description, " + 
			"       l.unitPosition, l.isexternal " + 
			"FROM data.locations l";
	
	public TMSLocation(ArtDataManagerService manager) {
		this.manager=manager;
	}
	
	private TMSLocation(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(rs);
		this.manager = manager;
	}
	
	@Override
    public ArtEntity factory(final ResultSet rs) throws SQLException 
    {
        return new TMSLocation(getManager(), rs);
    }
	
	@Override
	public Place getPlace() {
		return this.getManager().fetchByTMSLocationID(getLocationID());
	}
	
	
	
	
	private static StringFilter emptyFilter = new EmptyFilter();

    private ArtDataManagerService manager=null;
	
	private Long fingerprint = null;
	
	public ArtDataManagerService getManager() {
		return manager;
	}
	
	private void setManager(ArtDataManagerService manager) {
		this.manager = manager;
	}
	
	public DataSourceService getDataSourceService() {
		return getManager().getDataSourceService();
	}
	
	public void setAdditionalProperties(ResultSet rs) throws SQLException {
	}
	
	public String freeTextSearchToNodePropertyName(Object field) {
		return null;
	}

	Long entityID = null;
	public void setEntityID(Long entityID) {
		this.entityID = entityID;
	}
	public Long getEntityID() {
		return this.entityID;
	}
	
	public String getEntityKey() {
		return null;
	}
	
	public String getEntityUniqueID() {
		return getEntityKey() + ":" + getEntityID();
	}
	
	public String getJCREntityType() 
	{
		return this.getClass().getSimpleName();
	}
	
	public boolean sameFingerprint(FingerprintedEntity d) {
		return this.getFingerprint().equals(d.getFingerprint());
	}
	
	public Long getFingerprint() {
		return fingerprint;
	}
}
