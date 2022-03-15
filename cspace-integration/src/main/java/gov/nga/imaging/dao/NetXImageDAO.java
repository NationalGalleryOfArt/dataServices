package gov.nga.imaging.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gov.nga.common.imaging.NGAImage;

public interface NetXImageDAO {
    
    public List<String> PRIMARY_TABLE_COLUMNS = 
            Arrays.asList(new String[] {"uuid", "assetname", "folders", "iiifformat", "viewtype", "sequence", "width", 
                    "height", "maxpixels", "authorizations", "ispublic", "created", "modified", "assistivetext", 
                    "iiiffilesizeinbytes", "depictstmsobjectid", "ri_altmedium", "ri_altimageref", "ri_qualifier", 
                    "ri_isdetail", "ri_entitytype", "ri_entityid", "ri_photocredit", "ri_altdisplaydate", "ri_alttitle", 
                    "ri_projectid", "ri_viewsubtype", "ri_altattribution", "ri_altcreditline", "ri_relatedtmsobjectid", 
                    "ri_iszoomable"});

    public List<NGAImage> getImagesForObject(long objectID);
    
    public List<NGAImage> getImageByQuery(Map<String, Object> params);
    
    public NGAImage getImageByID(String id);
}
