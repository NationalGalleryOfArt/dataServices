/*
    NGA Art Data API: Art Data Services Interface
    This interface represents the calls used by NGA web sites to locate
    art entity data via various searches.  Implementations of this
    interface are the typical entry point into the NGA art data sets.

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

import gov.nga.common.suggest.Suggestion;
import gov.nga.entities.art.factory.ArtObjectFactory;
import gov.nga.entities.art.factory.ConstituentFactory;
import gov.nga.common.search.Facet;
import gov.nga.common.search.FacetHelper;
import gov.nga.common.search.FreeTextSearchable;
import gov.nga.common.search.ResultsPaginator;
import gov.nga.common.search.SearchHelper;
import gov.nga.common.search.SortHelper;
import gov.nga.utils.ConfigService;
import gov.nga.utils.db.DataSourceService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.nga.common.entities.art.ArtDataCacher;
import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.OperatingMode;

public interface ArtDataManagerService {
	
	// JDBC Pool Service 
	public DataSourceService getDataSourceService();
    
	// fetch configuration services reference
	public ConfigService getConfig();

    public Long synchronizationFinishedAt();

    // the operating mode of the APIs
	public OperatingMode getOperatingMode();
	
	public ArtDataQuerier getArtDataQuerier();
	
	public ArtDataCacher getArtDataCacher();

	public boolean isDataReady(boolean throwExceptionIfNot);
}
