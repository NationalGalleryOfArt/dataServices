package gov.nga.entities.art;

import gov.nga.entities.common.FingerprintedEntity;
import gov.nga.search.SearchFilter;
import gov.nga.search.SortOrder;
import gov.nga.search.Sorter;
import gov.nga.utils.db.DataSourceService;
import gov.nga.utils.stringfilter.EmptyFilter;
import gov.nga.utils.stringfilter.StringFilter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class ArtEntityImpl extends FingerprintedEntity implements ArtEntity
{
	//private static final Logger log = LoggerFactory.getLogger(ArtEntity.class);

	private static SortOrder noDefaultSort = new SortOrder();
    private static StringFilter emptyFilter = new EmptyFilter();

    private ArtDataManagerService manager=null;
    
	protected ArtEntityImpl(ArtDataManagerService manager) {
		this.manager=manager;
	}
	
	protected ArtEntityImpl(ArtDataManagerService manager, Long fingerprint) {
		super(fingerprint);
		setManager(manager);
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

	public SortOrder getDefaultSortOrder() {
		return noDefaultSort;
	}

	public SortOrder getNaturalSortOrder() {
		return noDefaultSort;
	}

	public Long matchesAspect(Object ao, Object order) {
		return null;
	}
	
	public int aspectScore(Object ao, Object order, String matchString) {
		return Sorter.NULL;
	}
	
	public Boolean matchesFilter(SearchFilter filter) {
		return false;
	}
    
	public List<String> getFacetValue(Object o) {
		return null;
	}
	
	public void setAdditionalProperties(ResultSet rs) throws SQLException {
	}
	
	public String freeTextSearchToNodePropertyName(Object field) {
		return null;
	}

	public Long getEntityID() {
		return null;
	}

	public String getJCREntityType() 
	{
		return this.getClass().getSimpleName();
	}

    protected StringFilter getDefaultFilter()
    {
        return emptyFilter;
    }

    
/*	private static String tmsDateToDisplayDate(String tmsDate) {
		boolean bc = false;
		if (tmsDate.equals("0"))
			return null;
		if (tmsDate.contains("-")) {
			tmsDate.replaceAll("-", "");
			bc = true;
		}
		if (bc) {
			return tmsDate+ " BC";
		}
		return tmsDate;
	}
*/	
}
