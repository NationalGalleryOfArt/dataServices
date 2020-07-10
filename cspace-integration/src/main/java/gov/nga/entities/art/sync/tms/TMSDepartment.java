package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.common.entities.art.ArtEntity;
import gov.nga.common.entities.art.Department;
import gov.nga.common.utils.TypeUtils;

public class TMSDepartment extends Department 
{
    
    protected TMSDepartment()
    {
        super();
    }

    
    public ArtEntity factory(ResultSet rs) throws SQLException 
    {
        final TMSDepartment cand = new TMSDepartment();
        cand.id = TypeUtils.getLong(rs, 2);
        cand.title = rs.getString(3);
        cand.code = rs.getString(4);
        return cand;
    }
    
}
