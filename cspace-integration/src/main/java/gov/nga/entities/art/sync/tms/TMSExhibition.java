package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.entities.art.ExhibitionConstituent;
import gov.nga.common.entities.art.ExhibitionStatus;
import gov.nga.common.entities.art.ExhibitionVenue;
import gov.nga.entities.art.TMSFetcher;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibition extends Exhibition 
{
    
    protected TMSExhibition (final ResultSet rs) throws SQLException, ParseException
    {
        id = TypeUtils.getLong(rs, 1);
        title = rs.getString(2);
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
        status = getStatusFromString(rs.getString(5));
    }
    
    protected TMSExhibition(Exhibition source)
    {
        super(source);
    }
    
    private static ExhibitionStatus getStatusFromString(final String cand)
    {
        ExhibitionStatus status = null;
        if (StringUtils.isNotBlank(cand))
        {
            if (cand.equals("external_nls"))
            {
                status = ExhibitionStatus.EXTERNAL_NLS;
            }
            else if (cand.equals("external"))
            {
                status = ExhibitionStatus.EXTERNAL;
            }
            else if (cand.equals("conceptual"))
            {
                status = ExhibitionStatus.CONCEPTUAL;
            }
            else if (cand.equals("on_hold"))
            {
                status = ExhibitionStatus.ON_HOLD;
            }
            else if (cand.equals("active"))
            {
                status = ExhibitionStatus.ACTIVE;
            }
            else if (cand.equals("early_planning"))
            {
                status = ExhibitionStatus.EARLY_PLANNING;
            }
            else if (cand.equals("closed"))
            {
                status = ExhibitionStatus.CLOSED;
            }
        }
        return status;
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
    public String getTitle() 
    {
        return title;
    }

    @Override
    public Long getID() 
    {
        return id;
    }

    @Override
    public ExhibitionStatus getStatus() 
    {
        return status;
    }

    protected void addVenue(final ExhibitionVenue venue)
    {
        venues.add(venue);
    }
    
    @Override
    public List<ExhibitionVenue> getVenues() 
    {
        return CollectionUtils.newArrayList(venues);
    }

    protected void addArtObject(final ExhibitionArtObject obj)
    {
        exhObjects.add(obj);
    }
    @Override
    public List<ExhibitionArtObject> getExhibitionObjects() 
    {
        return CollectionUtils.newArrayList(exhObjects);
    }

    @Override
    public Exhibition createObject(Exhibition source) 
    {
        
        return new TMSExhibition(source);
    }

    protected void addConstituent(final ExhibitionConstituent obj)
    {
        exhConstituents.add(obj);
    }
    
    @Override
    public List<ExhibitionConstituent> getConstituents() 
    {
        return CollectionUtils.newArrayList(exhConstituents);
    }

}
