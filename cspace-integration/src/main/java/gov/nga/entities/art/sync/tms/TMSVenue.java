package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionVenue;
import gov.nga.entities.art.TMSFetcher;
import gov.nga.common.utils.TypeUtils;

public class TMSVenue extends ExhibitionVenue 
{
    static final Logger LOG = LoggerFactory.getLogger(TMSVenue.class);
    
    
    private final TMSExhibition exhibition;
    private final Long constituent;
    private final Date openDate;
    private final Date closeDate;
    private final Boolean approved;
    private final String code;
    
    
    protected TMSVenue(final ResultSet rs, final Map<Long, Constituent> constituentMap, final Map<Long, Exhibition> exhbitionMap) throws SQLException, ParseException
    {
        exhibition = (TMSExhibition)exhbitionMap.get(TypeUtils.getLong(rs, 1));
        if (exhibition != null)
        {
            //LOG.info(String.format("Fetching exhibit with id %d: %s", TypeUtils.getLong(rs, 1), exhibition));
            constituent = TypeUtils.getLong(rs, 2);
            String dateString = rs.getString(3);
            if (StringUtils.isNotBlank(dateString))
            {
                openDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
            }
            else
            {
                openDate = null;
            }
            dateString = rs.getString(4);
            if (StringUtils.isNotBlank(dateString))
            {
                closeDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
            }
            else 
            {
                closeDate = null;
            }
            approved = TypeUtils.longToBoolean(TypeUtils.getLong(rs, 5));
            code = rs.getString(6);
            exhibition.addVenue(this);
        }
        else
        {
            throw new NullPointerException(String.format("No exhibition found with id: %s", TypeUtils.getLong(rs, 2)));
        }
    }

    @Override
    public Exhibition getExhbition() 
    {
        return exhibition;
    }

    @Override
    public Long getConstituentID() 
    {
        return constituent;
    }

    @Override
    public Date getOpenDate() 
    {
        return openDate;
    }

    @Override
    public Date getCloseDate() 
    {
        return closeDate;
    }

    @Override
    public Boolean getIsApproved() 
    {
        return approved;
    }

    @Override
    public String getCode() 
    {
        return code;
    }

}
