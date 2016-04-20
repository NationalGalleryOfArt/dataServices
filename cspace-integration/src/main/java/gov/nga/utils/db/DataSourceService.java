package gov.nga.utils.db;

import gov.nga.utils.CollectionUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Beaudet
 *
 */
public abstract class DataSourceService {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceService.class);
    
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
    
	// cache of connections
    Map<String,Connection> connectionMap = CollectionUtils.newHashMap();
        
    //TODO potentially add support for multiple names and connection pooling if desired
    public Connection getConnection(String dataSourceName) throws SQLException {
    	// check cache and return connection if valid, otherwise generate a new one and replace in cache
    	
    	// TODO - make this a pooled connection class and overwrite close so that it isn't closable
    	Connection c = connectionMap.get(dataSourceName);
    	if ( (c != null) && (!c.isClosed()) && c.isValid(5) )
    		return c;

    	try {
    		Class.forName(getDriverClassName());
    		c = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
    		connectionMap.put(dataSourceName, c);
    	}
    	catch (ClassNotFoundException ce) {
    		log.error("Could not acquire class " + getDriverClassName() + " for the specified database driver. " + ce.getMessage());
    	}
   		
        return c;
    }
    
    public void closeAll() {
    	for (String s : connectionMap.keySet()) {
    		Connection c = connectionMap.get(s);
    		if (c != null) {
    			try {
    				if ( !c.isClosed() )
    					c.close();
    			}
    			catch (SQLException se) {
    				log.warn("Minor problem detected when closing connection: " + se.getMessage());
    			}
    			connectionMap.remove(s);
    		}
    	}
    	log.info("finished closing database connections");
    }
    
}