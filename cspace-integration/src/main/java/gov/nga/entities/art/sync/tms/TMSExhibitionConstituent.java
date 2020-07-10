package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import gov.nga.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionConstituent;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibitionConstituent extends ExhibitionConstituent
{
    private final Exhibition exhibition;
    private final Long constituent;
    private final Long displayOrder;
    private final ROLE role;
    private final ROLETYPE roleType;

    protected TMSExhibitionConstituent(final ResultSet rs, final Map<Long, Exhibition> exhibitions, final Map<Long, Constituent> constituents) throws SQLException
    {
        exhibition = exhibitions.get(TypeUtils.getLong(rs, 2));
        if (exhibition == null) throw new RuntimeException(String.format("No exhibition with id %d", TypeUtils.getLong(rs, 2)));
        constituent = TypeUtils.getLong(rs, 3);
        Constituent obj = constituents.get(constituent);
        if (obj == null) throw new RuntimeException(String.format("No constituent with id %d", TypeUtils.getLong(rs, 3)));
        displayOrder = TypeUtils.getLong(rs, 4);
        role = getRoleFromString(rs.getString(5));
        roleType = getRoleTypeFromString(rs.getString(6));
    }
    
    private static ROLE getRoleFromString(final String cand)
    {
        ROLE role = null;
        if (StringUtils.isNotBlank(cand))
        {
            try
            {
                role = ROLE.valueOf(cand.toUpperCase());
            }
            catch(Exception err)
            {
                //do nothing
            }
        }
        return role;
    }
    
    private static ROLETYPE getRoleTypeFromString(final String cand)
    {
        ROLETYPE type = null;
        if (StringUtils.isNotBlank(cand))
        {
            try
            {
                type = ROLETYPE.valueOf(cand.toUpperCase());
            }
            catch(Exception err)
            {
                //do nothing
            }
        }
        return type;
    }
    
    @Override
    public Exhibition getExhibition()  
    {
        return exhibition;
    }

    @Override
    public Long getConstituentID()  
    {
        return constituent;
    }

    @Override
    public Long getDisplayOrder()  
    {
        return displayOrder;
    }

    @Override
    public ROLE getRole() 
    {
        return role;
    }

    @Override
    public ROLETYPE getRoleType() 
    {
        return roleType;
    }
    
    @Override
    public String toString()
    {
        return String.format("Constituent %d for Exhibition %s", getConstituentID(), getExhibition());
    }
}
