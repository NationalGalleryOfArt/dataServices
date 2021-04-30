package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.ArtObject;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.entities.art.ExhibitionLoan;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibitionArtObject extends ExhibitionArtObject
{
    private final ArtObject artObject;
    private final TMSExhibition exhibition;
    
    protected static TMSExhibitionArtObject getObjectFromSQL(final ResultSet rs, 
    		final Map<Long, Exhibition> exhibitions, final Map<Long, ArtObject> artObjects, 
    		final Map<Long, ExhibitionLoan> loans, final ArtDataQuerier manager) throws SQLException
    {
        final String title = rs.getString(4);
        final String catalogNumber = rs.getString(5);
        final String dexID = rs.getString(6);
        final String medium = rs.getString(7);
        final String displayDate = rs.getString(8);
        final String creditLine = rs.getString(9);
        final String dimensions = rs.getString(10);
        final String attribution = rs.getString(11);
        final String attributionInverted = rs.getString(12);
        final String photoRestrictions = rs.getString(13);
        Long rowCount;
        final Long venueCount = ((rowCount = TypeUtils.getLong(rs, 14)) == null) ? 0L : rowCount;
        final String venueCodes = rs.getString(15);
        final Boolean hasExhibitionRights = TypeUtils.longToBoolean(TypeUtils.getLong(rs, 16));
        final LOAN_STATUS objectLoanStatus = TypeUtils.getEnumValue(LOAN_STATUS.class, rs.getString(17));
        
        final TMSExhibition exhibition = (TMSExhibition) exhibitions.get(TypeUtils.getLong(rs, 1));
        

        if (exhibition != null)
        {
            final ArtObject artObject = artObjects.get(TypeUtils.getLong(rs, 2));
            if (artObject != null)
            {
            	final ExhibitionLoan loan = loans.get(TypeUtils.getLong(rs, 3));
                final TMSExhibitionArtObject exhObj = new TMSExhibitionArtObject(loan, objectLoanStatus, venueCount, hasExhibitionRights, venueCodes, title, catalogNumber,
    				dexID, medium, displayDate, creditLine, dimensions, attribution, attributionInverted, photoRestrictions,
    				exhibition, artObject, manager);
                exhibition.addArtObject(exhObj);
                artObject.addExhibition(exhibition);
                return exhObj;
            }
            throw new NullPointerException(String.format("No ArtObject found with id: %s", TypeUtils.getLong(rs, 2)));
        }
        throw new NullPointerException(String.format("No exhibition found with id: %s", TypeUtils.getLong(rs, 1)));
        
    }
    
    protected TMSExhibitionArtObject(ExhibitionLoan loan, LOAN_STATUS loanStatus,
			Long venueCount, boolean hasExhibitionRights, String venueCodes, String title, String catalogNumber,
			String dexID, String medium, String displayDate, String creditLine, String dimensions, String attribution,
			String attributionInverted, String photoRestrictions, TMSExhibition exhibition,
			ArtObject artObject, ArtDataQuerier manager) 
	{
		super(artObject.getObjectID(), exhibition.getID(), loan, loanStatus, venueCount, hasExhibitionRights, venueCodes, title, catalogNumber,
				dexID, medium, displayDate, creditLine, dimensions, attribution, attributionInverted, photoRestrictions, manager);
		this.artObject = artObject;
		this.exhibition = exhibition;
	}

	@Override
	public ArtObject getArtObject() 
	{
		return artObject;
	}


    @Override
    public Exhibition getExhibition() 
    {
        return exhibition;
    }

	@Override
	public TMSExhibitionArtObject factory(ResultSet rs) throws SQLException {
		return null;
	}

}
