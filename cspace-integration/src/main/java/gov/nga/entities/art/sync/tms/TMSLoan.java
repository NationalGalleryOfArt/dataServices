package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import gov.nga.entities.art.Constituent;
import gov.nga.common.entities.art.ExhibitionLoan;
import gov.nga.entities.art.TMSFetcher;
import gov.nga.common.utils.TypeUtils;

public class TMSLoan extends ExhibitionLoan
{
    private final Long constituent;
    private final Long id;
    private final String number;
    private final PURPOSE purpose;
    private final CATEGORY category;
    private final ROLE role;
    private final STATUS status;
    private final TYPE type;
    private final Date startDate;
    private final Date endDate;
    
    protected TMSLoan(final ResultSet rs, final Map<Long, Constituent> constituentMap) throws SQLException, ParseException
    {
        id = TypeUtils.getLong(rs, 1);
        number = rs.getString(2);
        purpose = TypeUtils.getEnumValue(PURPOSE.class, rs.getString(3));
        category = TypeUtils.getEnumValue(CATEGORY.class, rs.getString(4));
        type = TypeUtils.getEnumValue(TYPE.class, rs.getString(5));
        status = TypeUtils.getEnumValue(STATUS.class, rs.getString(6));
        role = TypeUtils.getEnumValue(ROLE.class, rs.getString(7));
        String dateString = rs.getString(8);
        if (StringUtils.isNotBlank(dateString))
        {
            startDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
        }
        else 
        {
            startDate = null;
        }
        
        dateString = rs.getString(9);
        if (StringUtils.isNotBlank(dateString))
        {
            endDate = TMSFetcher.DATE_FORMATTER.parse(dateString);
        }
        else
        {
            endDate = null;
        }
        constituent = TypeUtils.getLong(rs, 10);
    }

    @Override
    public Long getID() 
    {
        return id;
    }

    @Override
    public String getNumber() 
    {
        return number;
    }
    
    @Override
    public PURPOSE getPurpose() 
    {
        return purpose;
    }

    @Override
    public CATEGORY getCategory() 
    {
        return category;
    }

    @Override
    public TYPE getType() 
    {
        return type;
    }

    @Override
    public STATUS getStatus() 
    {
        return status;
    }

    @Override
    public Date getStartDate() 
    {
        return startDate;
    }

    @Override
    public Date getEndDate() 
    {
        return endDate;
    }

    @Override
    public Long getConstituentID() 
    {
        return constituent;
    }

    @Override
    public ROLE getRole() 
    {
        return role;
    }

}
