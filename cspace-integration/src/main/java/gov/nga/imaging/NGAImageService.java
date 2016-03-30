package gov.nga.imaging;


import java.sql.SQLException;
import java.util.List;


public interface NGAImageService
{
    List<NGAImage> getImages(Imagery.PROJECT project, Imagery.ENTITY_TYPE objectType, String objectId, Imagery.DISPLAYTYPE displayType) throws SQLException;
    
    NGAImage getImage(Imagery.PROJECT project, Imagery.ENTITY_TYPE objectType, String objectId, Imagery.DISPLAYTYPE displayType, String sequence) throws SQLException;
    
}
