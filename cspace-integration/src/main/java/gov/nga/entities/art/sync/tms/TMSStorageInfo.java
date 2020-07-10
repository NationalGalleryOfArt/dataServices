package gov.nga.entities.art.sync.tms;

import java.util.Date;

import gov.nga.common.entities.art.ArtObjectStorageInfo;

public class TMSStorageInfo extends ArtObjectStorageInfo
{
    protected TMSStorageInfo(String cn, String pn, Long icl, Date ed, Date bd, Date endD) 
    {
        super(cn, pn, icl, ed, bd, endD);
    }

}
