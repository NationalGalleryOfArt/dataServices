package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Location;
import gov.nga.common.entities.art.TMSFetcher;
import gov.nga.utils.db.DataSourceService;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.TypeUtils;

public class ArtObjectLocationFactory implements TMSEntityFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(ArtObjectLocationFactory.class);
    
    private Map<Long, ArtObject> artObjectMap;
    private Map<Long, Location> locationsMap;
    private Set<Long> setArtObjects;
    private TMSFetcher fetcher;
    
    synchronized public void buildLocations(final Map<Long, ArtObject> artObjectMap, 
                final Map<Long, Location> locations, final DataSourceService ps, 
                final TMSFetcher fetcher) throws SQLException
    {
        this.artObjectMap = artObjectMap;
        this.locationsMap = locations;
        this.fetcher = fetcher;
        LOG.debug(String.format("Is set for private information: %b", fetcher.getIsPrivateConfigured()));
        if (fetcher.getIsPrivateConfigured())
        {
            setArtObjects = CollectionUtils.newTreeSet();
            final TMSQuerier querier = new TMSQuerier(ps);
            querier.getQueryResults(TMSQuery.COMPONENT_LOCATION.getConstantValue(), this);
            //LOG.debug(String.format("Object with current and home locations set: %s", setArtObjects));
        }
        else
        {
            setPublicLocations();
        }
    }
    
    private void setPublicLocations()
    {
        for (ArtObject obj: artObjectMap.values())
        {
            if (obj.getLocationID() != null && obj.getLocationID() > -1)
            {
                fetcher.setCurretnLocation(obj, new TMSArtObjectLocation(locationsMap.get(obj.getLocationID()), null));
            }
        }
    }
    

    @Override
    public void processResult(ResultSet rs) throws SQLException 
    {
        /****
         * 
         * The complete location records is comprised of the location info from locations map
         * and the object specific storage information. The storage information is kept in a 
         * "storage history" table which is not designed for targeted queries for the most current 
         * history record/row. So we do that here by checking to see if a location has already been set
         * and skipping any results when the record has already been set
         * 
         */
        try
        {
            final Long objectID = rs.getLong(7);
            final Long locID = rs.getLong(8);
            if (!setArtObjects.contains(objectID))
            {
                final ArtObject obj = artObjectMap.get(objectID);
                if (objectID == 1867L)
                {
                    LOG.info(String.format("Location row for id [%d] locid = %d homelocid = %d row = %d", objectID, obj.getLocationID(), obj.getHomeLocationID(), locID));
                }
                if (obj != null)
                {   
                    //Start by checking currentLocation
                    if (obj.getLocationID() != null && obj.getLocationID().equals(locID) && obj.getLocation() == null)
                    {
                        fetcher.setCurretnLocation(obj, new TMSArtObjectLocation
                                            (
                                             locationsMap.get(locID),
                                             new TMSStorageInfo(rs.getString(1), rs.getString(2),
                                                     TypeUtils.getLong(rs, 3), TypeUtils.getDate(rs, 4, TMSFetcher.DATE_FORMATTER),
                                                     TypeUtils.getDate(rs, 5, TMSFetcher.DATE_FORMATTER),
                                                     TypeUtils.getDate(rs, 6, TMSFetcher.DATE_FORMATTER))
                                             ));
                    }
                    if (obj.getHomeLocationID() != null && obj.getHomeLocationID().equals(locID) && obj.getHomeLocation() == null)
                    {
                        fetcher.setHomeLocation(obj, new TMSArtObjectLocation
                                (
                                 locationsMap.get(locID),
                                 new TMSStorageInfo(rs.getString(1), rs.getString(2),
                                         TypeUtils.getLong(rs, 3), TypeUtils.getDate(rs, 4, TMSFetcher.DATE_FORMATTER),
                                         TypeUtils.getDate(rs, 5, TMSFetcher.DATE_FORMATTER),
                                         TypeUtils.getDate(rs, 6, TMSFetcher.DATE_FORMATTER))
                                 ));
                    }
                    
                    if (obj.getLocation() != null && obj.getHomeLocation() != null)
                    {
                        setArtObjects.add(objectID);
                        if (objectID == 1867L)
                        {
                            LOG.info(String.format("%s: curloc: %s homeloc: %s", obj, obj.getLocation(), obj.getHomeLocation()));
                        }
                    }
                }
                
            }
        }
        catch (final Exception err)
        {
            LOG.warn(String.format("Caught exception processing result %s[%s]:%s", rs.getString(7), rs.getString(8), rs.getString(5)), err);
        }
        
        
    }

}
