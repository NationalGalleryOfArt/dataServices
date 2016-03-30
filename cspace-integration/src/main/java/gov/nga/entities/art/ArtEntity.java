package gov.nga.entities.art;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import gov.nga.search.SortOrder;
import gov.nga.entities.common.Factory;
import gov.nga.entities.common.FingerprintedInterface;
import gov.nga.search.Faceted;
import gov.nga.search.SearchFilter;
import gov.nga.search.Searchable;
import gov.nga.search.Sortable;
import gov.nga.utils.db.DataSourceService;

public interface ArtEntity 
	extends Factory<ArtEntity>, Searchable, Sortable, Faceted, FingerprintedInterface { 
 
	public ArtDataManagerService getManager();
	
	public DataSourceService getDataSourceService();
	
	public SortOrder getDefaultSortOrder();
	
	public SortOrder getNaturalSortOrder();
	
	public Long matchesAspect(Object ao, Object order);
	
	public Integer aspectScore(Object ao, Object order, String matchString);
	
	public Boolean matchesFilter(SearchFilter filter);
	
	public List<String> getFacetValue(Object o);
	
	public void setAdditionalProperties(ResultSet rs) throws SQLException;
	
	public Long getEntityID();
	
	public String freeTextSearchToNodePropertyName(Object field);
	
	public String getJCREntityType ();
}
