package gov.nga.jdbc;


import gov.nga.utils.db.DataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *  Copyright 2016 -- National Gallery of Art
 *  4th and Constitution Avenue NW
 *  Washington, DC 20565
 *  United States
 *  All Rights Reserved.
 *
 *  This software is the confidential and proprietary information of
 *  the National Gallery of Art (NGA), ("Confidential Information"). You shall
 *  not disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with the NGA.
 *
 *  ==============================================================================
 *
 *    Jdbc Template
 *
 *    JdbcTemplate class executes SQL queries, initiating iteration over ResultSets and
 *    catching SQLException exceptions.
 *
 *  ==============================================================================
 */

public class JdbcTemplate
{

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSourceService dataSource;

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     *
     * @param dataSource - the JDBC DataSource to obtain connections from
     */
    public JdbcTemplate(DataSourceService dataSource)
    {
        if (dataSource==null)
            throw new IllegalArgumentException("Datasource cannot be null");

        this.dataSource = dataSource;
    }

    /**
     * Query given SQL to create a prepared statement from SQL and a list of
     * arguments to bind to the query, mapping a single result row to a Java object via a RowMapper.
     *
     * @param sql query to execute
     * @param args - arguments to bind to the query
     * @param rowMapper - object that will map one object per row
     * @return the result object of the required type, or null in case of SQL NULL
     * @throws SQLException when encountered an error trying to fetch database data
     */
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            try
            {
                conn = dataSource.getConnection();
                if (conn != null)
                {
                    ps = conn.prepareStatement(sql);
                    setParameters(ps, args);
                    rs = ps.executeQuery();
                    if (rs != null && rs.next())
                    {
                        return rowMapper.mapRow(rs);

                    }
                } else
                {
                    LOGGER.error("Could not acquire connection to datasource with name " + dataSource.getUrl());
                    throw new SQLException("Could not acquire connection to datasource with name " + dataSource.getUrl());
                }
            } finally
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            }

        } catch (SQLException se)
        {
            LOGGER.error("Encountered an error trying to fetch database data ", se.getMessage());
            throw se;
        }
        return null;
    }

    /**
     * Execute a query given SQL, mapping each row to a Java object via a RowMapper.
     *
     * @param sql query to execute
     * @param args - arguments to bind to the query
     * @param rowMapper - object that will map one object per row
     * @return the result List, containing mapped objects
     * @throws SQLException when encountered an error trying to fetch database data
     */
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws SQLException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> result = new ArrayList<T>();
        try
        {
            try
            {
                conn = dataSource.getConnection();
                if (conn != null)
                {
                    ps = conn.prepareStatement(sql);
                    if (args!=null && args.length>0) {
                        setParameters(ps, args);
                    }
                    rs = ps.executeQuery();
                    if (rs != null)
                    {
                        while (rs.next())
                        {
                            T o = rowMapper.mapRow(rs);
                            result.add(o);
                        }

                    }
                } else
                {
                    LOGGER.error("Could not acquire connection to datasource with name " + dataSource.getUrl());
                    throw new SQLException("Could not acquire connection to datasource with name " + dataSource.getUrl());
                }
            } finally
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            }

        } catch (SQLException se)
        {
            LOGGER.error("Encountered an error trying to fetch database data ", se.getMessage());
            throw se;
        }
        return result;
    }

    /**
     * Set a args on the PreparedStatement.
     *
     * @param ps PreparedStatement
     * @param args - arguments
     * @throws SQLException when an unsupported argument type is set
     */
    private void setParameters(PreparedStatement ps, Object[] args) throws SQLException
    {
        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                if (CharSequence.class.isAssignableFrom(arg.getClass()))
                    ps.setString(i+1, arg.toString());
                else if (java.util.Date.class.isAssignableFrom(arg.getClass()))
                    ps.setTimestamp(i+1, new java.sql.Timestamp(((java.util.Date) arg).getTime()));
                else if (arg instanceof Calendar)
                {
                    Calendar cal = (Calendar) arg;
                    ps.setTimestamp(i+1, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                } else if (Long.class.isAssignableFrom(arg.getClass()))
                    ps.setLong(i+1, (Long) arg);
                else
                    throw new SQLException("Unsupported parameter type ["+arg.getClass()+"]");

            }
        }
    }
}
