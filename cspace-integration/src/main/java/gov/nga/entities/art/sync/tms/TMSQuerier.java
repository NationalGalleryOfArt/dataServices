package gov.nga.entities.art.sync.tms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.utils.db.DataSourceService;

public class TMSQuerier 
{
    private static final Logger log = LoggerFactory.getLogger(TMSQuerier.class);
    
    private final DataSourceService dataSourceService;
    
    protected TMSQuerier(final DataSourceService ds) 
    {
        dataSourceService = ds;
    }
    
    protected void getQueryResults(final String query, final TMSEntityFactory factory) throws SQLException
    {
        ResultSet rs = null;
        try ( Connection conn = dataSourceService.getConnection(); ) 
        {
            if (conn != null) {
                try ( PreparedStatement st = conn.prepareStatement(query);)
                {
                    rs = st.executeQuery(); 
                    log.debug(String.format("Query: %s has results: %b", query, rs != null));
                    if (rs != null)
                    {
                        while (rs.next())
                        {
                            factory.processResult(rs);
                        }
                    }
                    conn.close();
                }
            }
            else 
            {
                throw new SQLException("Could not acquire connection to datasource");
            }
        }
    }
}
