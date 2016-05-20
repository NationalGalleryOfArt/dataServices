package gov.nga.entities.art;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.entities.common.Factory;
import gov.nga.entities.common.FingerprintedInterface;
import gov.nga.utils.db.DataSourceService;

public interface ArtEntity extends Factory<ArtEntity>, FingerprintedInterface { 
	
	public enum OperatingMode {
		PUBLIC,
		PRIVATE
	}
 
	public ArtDataManagerService getManager();
	
	public DataSourceService getDataSourceService();
	
	public void setAdditionalProperties(ResultSet rs) throws SQLException;
	
	public Long getEntityID();
	
	public String freeTextSearchToNodePropertyName(Object field);
	
	public String getJCREntityType ();
}
