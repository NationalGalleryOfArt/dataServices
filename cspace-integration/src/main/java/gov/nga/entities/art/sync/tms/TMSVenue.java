package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionVenue;
import gov.nga.common.entities.art.TMSFetcher;
import gov.nga.common.utils.TypeUtils;

public class TMSVenue extends ExhibitionVenue 
{
    static final Logger LOG = LoggerFactory.getLogger(TMSVenue.class);
    
    private TMSExhibition exhibition;
    private Constituent constituent;
    
    
    protected static TMSVenue getVenueFromSQL(final ResultSet rs, final Map<Long, Constituent> constituentMap, final Map<Long, Exhibition> exhbitionMap) throws SQLException, ParseException
    {
    	final Long exhibitionID = TypeUtils.getLong(rs, 1);
        final TMSExhibition exhibition = (TMSExhibition)exhbitionMap.get(exhibitionID);
        if (exhibition != null)
        {
            //LOG.info(String.format("Fetching exhibit with id %d: %s", TypeUtils.getLong(rs, 1), exhibition));
            final Long constituentID = TypeUtils.getLong(rs, 2);
            final Constituent constituent = constituentMap.get(constituentID);
            
            String dateString = rs.getString(3);
            Date openDate = null;
            if (StringUtils.isNotBlank(dateString))
            {
                openDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
            }
            Date closeDate = null;
            dateString = rs.getString(4);
            if (StringUtils.isNotBlank(dateString))
            {
                closeDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
            }
            
            final boolean approved = TypeUtils.longToBoolean(TypeUtils.getLong(rs, 5));
            final String code = rs.getString(6);
            final TMSVenue venue = new TMSVenue (exhibitionID, constituentID, openDate, 
            		closeDate, approved, code, exhibition, constituent);
            exhibition.addVenue(venue);
            return venue;
        }
        throw new NullPointerException(String.format("No exhibition found with id: %s", TypeUtils.getLong(rs, 2)));
        
    }
    
    protected TMSVenue(Long eID, Long cID, Date oDate, Date cDate, 
			boolean approved, String code, TMSExhibition exhibition, Constituent constituent) 
    {
		super(eID, cID, oDate, cDate, approved, code);
		this.exhibition = exhibition;
		this.constituent = constituent;
	}

    @Override
    public Exhibition getExhbition() 
    {
        return exhibition;
    }

	@Override
	public Constituent getConstituent() 
	{
		return constituent;
	}

}
