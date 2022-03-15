package gov.nga.imaging.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nga.common.imaging.NGAImage;
import gov.nga.jdbc.RowMapper;

public class NetXImageRowMapper implements RowMapper<NGAImage> {

	private final String imageServerURL;
	
	public NetXImageRowMapper (final String serverURL)
	{
		imageServerURL = serverURL;
	}
	
    @Override
    public NGAImage mapRow(final ResultSet rs) throws SQLException
    {
        return new NGAImageLocal(rs, imageServerURL);
    }
}
