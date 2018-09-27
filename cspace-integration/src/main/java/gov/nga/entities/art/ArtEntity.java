/*
    NGA Art Data API: Art Entity interface from which all other art entities are sub-classed 

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
	
	// TODO - these are JCR specific and should be removed from a non-JCR implementation
	public String freeTextSearchToNodePropertyName(Object field);
	
	public String getJCREntityType ();
}
