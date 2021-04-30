package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.ExhibitionLoan;
import gov.nga.common.entities.art.TMSFetcher;
import gov.nga.common.utils.TypeUtils;

public class TMSLoan extends ExhibitionLoan
{
    private Constituent constituent;
    
    protected static TMSLoan getLoanFromSQL(final ResultSet rs, final Map<Long, Constituent> constituentMap) throws SQLException, ParseException
    {
        final Long id = TypeUtils.getLong(rs, 1);
        final String number = rs.getString(2);
        final PURPOSE purpose = TypeUtils.getEnumValue(PURPOSE.class, rs.getString(3));
        final CATEGORY category = TypeUtils.getEnumValue(CATEGORY.class, rs.getString(4));
        final TYPE type = TypeUtils.getEnumValue(TYPE.class, rs.getString(5));
        final STATUS status = TypeUtils.getEnumValue(STATUS.class, rs.getString(6));
        final ROLE role = TypeUtils.getEnumValue(ROLE.class, rs.getString(7));
        
        String dateString = rs.getString(8);
        Date startDate = null;
        if (StringUtils.isNotBlank(dateString))
        {
            startDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
        }
        dateString = rs.getString(9);
        Date endDate = null;
        if (StringUtils.isNotBlank(dateString))
        {
            endDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
        }
        final Long constituentID = TypeUtils.getLong(rs, 10);
        final Constituent constituent = constituentMap.get(constituentID);
        return new TMSLoan(id, constituentID, number, startDate, endDate, 
        		purpose, category, type, status, role, constituent);
    }
    
    protected TMSLoan(Long id, Long constituentID, String number, Date startDate, Date endDate, PURPOSE purpose,
			CATEGORY category, TYPE type, STATUS status, ROLE role, Constituent constituent) 
	{
		super(id, constituentID, number, startDate, endDate, purpose, category, type, status, role);
		this.constituent = constituent;
	}

	@Override
	public Constituent getConstituent() 
	{
		// TODO Auto-generated method stub
		return null;
	}

}
