package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.common.entities.art.AlternateNumberData;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.utils.TypeUtils;
import gov.nga.entities.art.ArtDataManagerService;
import gov.nga.common.entities.art.ArtEntity;
import gov.nga.entities.common.FingerprintedEntity;
import gov.nga.utils.db.DataSourceService;
import gov.nga.utils.stringfilter.EmptyFilter;
import gov.nga.utils.stringfilter.StringFilter;

public class TMSAlternateNumber extends  AlternateNumberData implements ArtEntity
{


    public static final String fetchAllAltNumbersQuery = 
                                "select objectID, altnumtype, altnum, fingerprint " +
                                "from data.objects_altnums order by objectID";   
	
    private static StringFilter emptyFilter = new EmptyFilter();

    private ArtDataManagerService manager=null;
	
	private Long fingerprint = null;
    
	public TMSAlternateNumber(ArtDataQuerier manager) {
		super(manager);
	}
	
	protected TMSAlternateNumber(ArtDataQuerier manager, Long fingerprint) {
		super(manager);
		this.fingerprint = fingerprint;
	}
	
    protected TMSAlternateNumber(final ArtDataQuerier manager, final ResultSet rs) throws SQLException
    {
    	super(TypeUtils.getLong(rs, 1), rs.getString(2), rs.getString(3), manager);
    }

    @Override
    public TMSAlternateNumber factory(final ResultSet rs) throws SQLException 
    {
        return new TMSAlternateNumber(getQueryManager(), rs);
    }
	
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
