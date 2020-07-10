package gov.nga.entities.art.sync.tms;

import gov.nga.common.entities.art.ArtObjectLocation;
import gov.nga.common.entities.art.ArtObjectStorageInfo;
import gov.nga.common.entities.art.Location;

public class TMSArtObjectLocation extends ArtObjectLocation
{
    protected TMSArtObjectLocation(Location loc, ArtObjectStorageInfo si) 
    {
        super(loc, si);
    }

}
