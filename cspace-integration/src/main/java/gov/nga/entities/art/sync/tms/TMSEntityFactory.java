package gov.nga.entities.art.sync.tms;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TMSEntityFactory 
{
    public void processResult(ResultSet rs) throws SQLException;
}
