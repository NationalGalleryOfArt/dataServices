/*
    NGA Art Data API: Base Art Entity Implementation from which other entity implementations are derived  

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

import gov.nga.entities.common.FingerprintedEntity;
import gov.nga.utils.db.DataSourceService;
import gov.nga.utils.stringfilter.EmptyFilter;
import gov.nga.utils.stringfilter.StringFilter;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ArtEntityImpl extends FingerprintedEntity implements ArtEntity
{
	//private static final Logger log = LoggerFactory.getLogger(ArtEntity.class);
	
//	private static SortOrder noDefaultSort = new SortOrder();
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
	
//	public SortOrder getDefaultSortOrder() {
//		return noDefaultSort;
//	}

//	public SortOrder getNaturalSortOrder() {
//		return noDefaultSort;
//	}

//	public Long matchesAspect(Object ao, Object order) {
//		return null;
//	}
	
//	public int aspectScore(Object ao, Object order, String matchString) {
//		return Sorter.NULL;
//	}
	
//	public Boolean matchesFilter(SearchFilter filter) {
//		return false;
//	}
    
//	public List<String> getFacetValue(Object o) {
//		return null;
//	}
	
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
