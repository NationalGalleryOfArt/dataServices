/*
    Utils: DataSourceService provides utilities for dealing with pooled DB connections
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gov.nga.utils.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Beaudet
 *
 */
public abstract class DataSourceService {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceService.class);
	static { log.info("DataSourceService loaded"); }
	
	public void init(String url, String username, String password, String validationQuery) {
		if ( url == null )
			return;
		
		setUrl(url);
		setUsername(username);
		setPassword(password);

		// First, we'll create a ConnectionFactory that the pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory, using the connect string passed in the command line
		// arguments.
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(getUrl(), getUsername(), getPassword());

		// Now we'll need a ObjectPool that serves as the actual pool of connections.
		// We'll use a GenericObjectPool instance, although any ObjectPool implementation will suffice.
		// I should note this is highly configurable and there are many options, but with the exception of
		// testing connections on borrow, we use the defaults
		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>();
		connectionPool.setTestOnBorrow(true);

		// Next we'll create the PoolableConnectionFactory, which wraps the "real" Connections created 
		// by the ConnectionFactory with the classes that implement the pooling functionality.
		PoolableConnectionFactory poolableConnectionFactory =
				new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);

		// Set the factory's pool property to the owning pool
		poolableConnectionFactory.setPool(connectionPool);

		// Finally, we create the PoolingDriver itself,
		// passing in the object pool we created.
		PoolingDataSource newDataSource = new PoolingDataSource(connectionPool);
		setDataSource(newDataSource);
	}
	
	private DataSource dataSource = null;
	private void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return this.dataSource;
	}

    private String driverClassName;
    public void setDriverClassName(String driverClassName) {
    	this.driverClassName = driverClassName;
    }
    public String getDriverClassName() {
    	return this.driverClassName;
    }
    
    private String url;
    public void setUrl(String url) {
		this.url = url;
	}
    public String getUrl() {
    	return this.url;
    }

    private String username;
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return this.username;
	}

	private String password;
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return this.password;
	}
    
    public Connection getConnection() throws SQLException {
    	return dataSource == null ? null : dataSource.getConnection();
    }

    // TODO - this seems to be unused and the dataSourceName certainly is not so at
    // best this is probably misleading and should be deprecated / removed if that's the case
    public Connection getConnection(String dataSourceName) throws SQLException {
    	return dataSource == null ? null :  dataSource.getConnection();
    }
    
}