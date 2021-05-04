package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import gov.nga.common.entities.art.ArtDataQuerier;
import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.Constituent;
import gov.nga.common.entities.art.Exhibition;
import gov.nga.common.entities.art.ExhibitionConstituent;
import gov.nga.common.utils.TypeUtils;

public class TMSExhibitionConstituent extends ExhibitionConstituent
{
    private Exhibition exhibition;
    private Constituent constituent;

    protected static TMSExhibitionConstituent getConstituentFromSQL(final ArtDataQuerier manager,
    		final ResultSet rs, final Map<Long, Exhibition> exhibitions, final Map<Long, Constituent> constituents) throws SQLException
    {
        final Exhibition exhibition = exhibitions.get(TypeUtils.getLong(rs, 2));
        if (exhibition == null) 
        {
        	throw new RuntimeException(String.format("No exhibition with id %d", TypeUtils.getLong(rs, 2)));
        }
        
        final Constituent obj = constituents.get(TypeUtils.getLong(rs, 3));
        if (obj == null) throw new RuntimeException(String.format("No constituent with id %d", TypeUtils.getLong(rs, 3)));
        final Long displayOrder = TypeUtils.getLong(rs, 4);
        final ROLE role = getRoleFromString(rs.getString(5));
        final ROLETYPE roleType = getRoleTypeFromString(rs.getString(6));
        return new TMSExhibitionConstituent(manager, displayOrder,
        					role, roleType, exhibition, obj);
    }
    
    protected TMSExhibitionConstituent(ArtDataQuerier manager, Long displayOrder, ROLE role, ROLETYPE roleType,
    						Exhibition exhibition, Constituent constituent) 
    {
		super(manager, exhibition.getID(), constituent.getConstituentID(), displayOrder, role, roleType);
		this.exhibition = exhibition;
		this.constituent = constituent;
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
    public String toString()
    {
        return String.format("Constituent %d for Exhibition %s", getConstituent(), getExhibition());
    }

	@Override
	public Constituent getConstituent() 
	{
		return constituent;
	}

	@Override
	public <T extends ArtEntity> T factory(ResultSet arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
