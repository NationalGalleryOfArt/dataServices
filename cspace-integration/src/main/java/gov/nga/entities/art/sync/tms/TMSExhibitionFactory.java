package gov.nga.entities.art.sync.tms;

import java.util.Map;

import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.utils.db.DataSourceService;

public interface TMSExhibitionFactory 
{
    public Map<Long, Exhibition> getExhibitions(Map<Long, ArtObject> artObjectMap, Map<Long, Constituent> constiuentMap, 
    		DataSourceService ps);
}
