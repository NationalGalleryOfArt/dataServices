package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import gov.nga.entities.art.ArtObject;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.entities.art.ExhibitionLoan;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibitionArtObject extends ExhibitionArtObject
{
    private final Long artObject;
    private final TMSExhibition exhibition;
    private final ExhibitionLoan loan;
    private final LOAN_STATUS objectLoanStatus;
    private final String attribution;
    private final String attributionInverted;
    private final String catalogNumber;
    private final String creditLine;
    private final String dexID;
    private final String dimensions;
    private final String displayDate;
    private final String medium;
    private final String photoRestrictions;
    private final String title;
    private final String venueCodes;
    private final Long venueCount;
    private final Boolean hasExhibitionRights;
    
    protected TMSExhibitionArtObject(final ResultSet rs, final Map<Long, Exhibition> exhibitions, final Map<Long, ArtObject> artObjects, final Map<Long, ExhibitionLoan> loans) throws SQLException
    {
        super();
        title = rs.getString(4);
        catalogNumber = rs.getString(5);
        dexID = rs.getString(6);
        medium = rs.getString(7);
        displayDate = rs.getString(8);
        creditLine = rs.getString(9);
        dimensions = rs.getString(10);
        attribution = rs.getString(11);
        attributionInverted = rs.getString(12);
        photoRestrictions = rs.getString(13);
        Long rowCount;
        venueCount = ((rowCount = TypeUtils.getLong(rs, 14)) == null) ? 0L : rowCount;
        venueCodes = rs.getString(15);
        hasExhibitionRights = TypeUtils.longToBoolean(TypeUtils.getLong(rs, 16));
        objectLoanStatus = TypeUtils.getEnumValue(LOAN_STATUS.class, rs.getString(17));
        
        exhibition = (TMSExhibition) exhibitions.get(TypeUtils.getLong(rs, 1));
        artObject = TypeUtils.getLong(rs, 2);
        loan = loans.get(TypeUtils.getLong(rs, 3));

        if (exhibition != null)
        {
            ArtObject obj = artObjects.get(artObject);
            if (obj != null)
            {
                exhibition.addArtObject(this);
                if (!obj.getExhibitions().contains(exhibition))
                {
                    obj.addExhibition(exhibition.getID());
                }
            }
            else
            {
                throw new NullPointerException(String.format("No ArtObject found with id: %s", TypeUtils.getLong(rs, 2)));
            }   
        }
        else
        {
            throw new NullPointerException(String.format("No exhibition found with id: %s", TypeUtils.getLong(rs, 1)));
        }
    }

    @Override
    public Long getArtObjectID() 
    {
        return artObject;
    }

    @Override
    public Exhibition getExhibition() 
    {
        return exhibition;
    }

    @Override
    public ExhibitionLoan getLoan() 
    {
        return loan;
    }

    @Override
    public String getVenueCodes() 
    {
        return venueCodes;
    }

    @Override
    public String getTitle() 
    {
        return title;
    }

    @Override
    public String getCatalogNumber() 
    {
        return catalogNumber;
    }

    @Override
    public String getDexID() 
    {
        return dexID;
    }

    @Override
    public String getMedium() 
    {
        return medium;
    }

    @Override
    public String getDisplayDate() 
    {
        return displayDate;
    }

    @Override
    public String getCreditLine() 
    {
        return creditLine;
    }

    @Override
    public String getDimensions() 
    {
        return dimensions;
    }

    @Override
    public String getAttribution() 
    {
        return attribution;
    }

    @Override
    public String getAttributionInverted() 
    {
        return attributionInverted;
    }

    @Override
    public String getPhotoRestrictions() 
    {
        return photoRestrictions;
    }

    @Override
    public Long getVenueCount() 
    {
        return venueCount;
    }
    
    @Override 
    public Boolean getHasExhibitionRights()
    {
        return hasExhibitionRights;
    }
    
    @Override
    public  LOAN_STATUS getObjectLoanStatus()
    {
        return objectLoanStatus;
    }

}
