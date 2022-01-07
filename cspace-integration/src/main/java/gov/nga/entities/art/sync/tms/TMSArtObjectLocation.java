package gov.nga.entities.art.sync.tms;

import gov.nga.common.entities.art.ArtObjectLocation;
import gov.nga.common.entities.art.ArtObjectStorageInfo;
import gov.nga.common.entities.art.Location;

public class TMSArtObjectLocation extends ArtObjectLocation
{
	final Location location;
	
    protected TMSArtObjectLocation(Location loc, ArtObjectStorageInfo si) 
    {
        super(null, loc.getLocationID(), si);
        location = loc;
    }
    
    @Override
    public Location getLocationInfo()
    {
        return location;
    }

}
