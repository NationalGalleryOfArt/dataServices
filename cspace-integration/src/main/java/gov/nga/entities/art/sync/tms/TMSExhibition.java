package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionArtObject;
import gov.nga.common.entities.art.ExhibitionConstituent;
import gov.nga.common.entities.art.ExhibitionStatus;
import gov.nga.common.entities.art.ExhibitionVenue;
import gov.nga.common.entities.art.TMSFetcher;
import gov.nga.common.utils.CollectionUtils;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibition extends Exhibition 
{
	protected static TMSExhibition getExhibitionFromSQL(final ResultSet rs, ArtDataQuerier manager) throws SQLException
    {
        final Long id = TypeUtils.getLong(rs, 1);
        final String title = rs.getString(2);
        
        String dateString = rs.getString(3);
        Date openDate = null;
        if (StringUtils.isNotBlank(dateString))
        {
            try {
				openDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        Date closeDate = null;
        dateString = rs.getString(4);
        if (StringUtils.isNotBlank(dateString))
        {
            try {
				closeDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        final ExhibitionStatus status = getStatusFromString(rs.getString(5));
        return new TMSExhibition(id, title, openDate, closeDate, status, 
        							CollectionUtils.newArrayList(),
        							CollectionUtils.newArrayList(),
        							CollectionUtils.newArrayList(), manager);
    }
    
    protected TMSExhibition(Exhibition source)
    {
        super(source);
    }
    
    protected TMSExhibition(Long id, String title, Date openDate, Date closeDate, ExhibitionStatus status,
			List<ExhibitionVenue> venues, List<ExhibitionArtObject> exhObjects,
			List<ExhibitionConstituent> exhConstituents, ArtDataQuerier manager) 
	{
		super(id, title, openDate, closeDate, status, venues, exhObjects, 
				exhConstituents, manager);
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

    protected void addVenue(final ExhibitionVenue venue)
    {
        venues.add(venue);
    }

    protected void addArtObject(final ExhibitionArtObject obj)
    {
        exhObjects.add(obj);
    }


    protected void addConstituent(final ExhibitionConstituent obj)
    {
        exhConstituents.add(obj);
    }

	@Override
	public TMSExhibition factory(ResultSet rs) throws SQLException
	{
		return getExhibitionFromSQL(rs, getQueryManager());
				
	}
}
