package gov.nga.entities.art;

import gov.nga.utils.db.DataSourceService;
import gov.nga.utils.CollectionUtils;
import gov.nga.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityQuery<E extends ArtEntity> { 
    
	private static enum DataMode {
		CREATE, FILL
	}
	
	private static final Logger log = LoggerFactory.getLogger(EntityQuery.class);
	private static final String DATASOURCENAME = "tmspublicextract";
	
	private DataSourceService dataSourceService;
	
	private EntityQuery() {
		super();
	}
	
	protected EntityQuery(DataSourceService ds) {
		this();
        dataSourceService = ds;
	}
	
	// load a new entity using the supplied query and either create it or append
	// the fetched properties to it
	@SuppressWarnings("unchecked")
	private E fetcher(long id, String query, E entity, DataMode mode) throws SQLException {
		Connection conn = null;
		PreparedStatement st = null;
		E o = null;
		ResultSet rs = null;
		
		long start = 0;
		long endGetConn = 0;
		long endPrep = 0;
		long endQuery = 0;
		long endProc = 0;
		
		String q = null;
		try {
			try {
				start = Calendar.getInstance().getTimeInMillis();
				conn = dataSourceService.getConnection(DATASOURCENAME);
				if (conn != null) {
					endGetConn = Calendar.getInstance().getTimeInMillis();
					q = query.replace("@@", " = ? ");
					st = conn.prepareStatement(q);
					endPrep = Calendar.getInstance().getTimeInMillis();
					st.setLong(1, id);
					rs = st.executeQuery();
					endQuery = Calendar.getInstance().getTimeInMillis();
					if ( rs != null && rs.next() ) {
						switch (mode) {
						case FILL:
							//log.error(query);
							//log.error("for ID: " + id);
							entity.setAdditionalProperties(rs);
							break;
						default:
							o = (E) entity.factory(rs);
							break;
						}
					}
				}
				else {
					throw new SQLException("Could not acquire connection to datasource with name: " + DATASOURCENAME);
				}
			}

			finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			}
			
		}
		catch (SQLException se) {
			log.error("Encountered an error trying to fetch art entity: " + se.getMessage());
            log.info("Query was: " + q);
			throw se;
		}
		endProc = Calendar.getInstance().getTimeInMillis();
		log.debug("Query was: " + query);
		log.debug("Connection ms: " + (endGetConn - start));
		log.debug("Prep Stmt  ms: " + (endPrep - endGetConn));
		log.debug("Query      ms: " + (endQuery - endPrep));
		log.debug("Processing ms: " + (endProc - endQuery));
		if (endQuery - endPrep > 500)
			log.debug("Long Query (>500 ms): " + query);
		return o;
	}

	// load a new entity using the supplied query and either create it or append
	// the fetched properties to it
	@SuppressWarnings("unchecked")
	protected List<E> fetchAll(String query, E entity) throws SQLException {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		List<E> list = CollectionUtils.newArrayList();
		
		try {
			try {
				conn = dataSourceService.getConnection(DATASOURCENAME);
				if (conn != null) {
					st = conn.prepareStatement(query);
					rs = st.executeQuery();
					if (rs != null) {
						while (rs.next()) {
							E o = (E) entity.factory(rs);
							list.add(o);
						}
					}
				}
				else {
					throw new SQLException("Could not acquire connection to datasource with name: " + DATASOURCENAME);
				}
			}
			catch (SQLException se) {
				log.error("Problem connecting to database: " + se.getMessage());
				throw se;
			}
			finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			}
		}
		catch (SQLException se) {
			log.error("Encountered an error trying to fetch art entity: " + se.getMessage());
			log.info("Query was: " + query);
			throw se;
		}
		return list;
	}

	
	// load additional properties using a SQL query into an existing art entity identified
	// by the ID provided - should probably rely on the getKeyValue() method of FingerprintedEntity, but 
	// we can change that later if it still makes sense - some of our queries might not map cleanly
	// to that mode
	protected void loadAdditionalProperties(long id, String query, E entity) throws SQLException {
		fetcher(id, query, entity, DataMode.FILL);
	}

	// fetch an entity by ID and return a new structure using the supplied entity's factory method
	protected E fetchAndCreate(long id, String query, E entity) throws SQLException {
		return fetcher(id, query, entity, DataMode.CREATE);
	}
	
	// fetch an entity by ID and append a new structure created using the supplied entity's factory method
	// to the supplied List
	protected void fetchAndCreate(long id, String query, E entity, List<E> list) throws SQLException {
		List<Long> ids = CollectionUtils.newArrayList();
		ids.add(id);
		fetchAndCreate(ids, query, entity, list);
	}

	// fetch and create multiple entities by IDs supplied in the list and append those entities to the 
	// list of entities provided
	@SuppressWarnings("unchecked")
	protected void fetchAndCreate(List<Long> ids, String query, E entity, List<E> list) throws SQLException {
		Connection conn = null;
		PreparedStatement st = null;
		E o = null;
		ResultSet rs = null;
		final int MAXPARAMS = 8192;
		
		long start = 0;
		
		String q = null;
		
		if (ids.size() < 1)
			return;
		
		try {
			try {
				start = Calendar.getInstance().getTimeInMillis();
				conn = dataSourceService.getConnection(DATASOURCENAME);
				if (conn != null) {
					// we have to split large numbers of IDs into multiple queries 
					// due to limitations in DB query capabilities and JDBC libraries
					int runs = ids.size() / MAXPARAMS + 1;
					int rems = ids.size() > MAXPARAMS ? ids.size() % MAXPARAMS : ids.size();
					int lastParms = -1;
					int pIdx = 0;

					for (int i=0; i<runs; i++) {
						int numParms = ( i == runs-1 ? rems : MAXPARAMS );

						if (st == null || lastParms != numParms) {
							lastParms = numParms;
							q = query.replace("@@", " IN (" + StringUtils.fillQueryParams(numParms) + ")");
							if (st != null)
								st.close();
							st = conn.prepareStatement(q);
						}

						// now, set the parameters for this query
						for (int idx=1; idx<=numParms; idx++) {
							st.setLong(idx, ids.get(pIdx++));
						}

						// now, perform the actual query
						rs = st.executeQuery();
						if (rs != null) {
							while (rs.next()) {
								o = (E) entity.factory(rs);
								list.add(o);
							}
						}
					}
				}
				else {
					throw new SQLException("Could not acquire connection to datasource with name: " + DATASOURCENAME);
				}
			}
			finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			}
		}
		catch (SQLException se) {
			log.error("Encountered an error trying to fetch art entity: " + se.getMessage());
			log.info("Query was: " + q);
			throw se;
		}
		log.debug("Query Processing ms: " + (Calendar.getInstance().getTimeInMillis() - start));
	}

	// fetch and create multiple entities based on the supplied query and add them
	// to a list provided by the caller
/*	protected static List<Long> IDsForQuery(String query, String stringData) {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		long start = 0;
		
		List<Long> list = CollectionUtils.newArrayList();
		
		try {
			try {
				start = Calendar.getInstance().getTimeInMillis();
				conn = TMSPublicExtract.getConnection();
				st = conn.prepareStatement(query);
				st.setString(1, stringData);

				// now, perform the actual query
				rs = st.executeQuery();
				if (rs != null) {
					while (rs.next()) {
						list.add(TypeUtils.getLong(rs, 1));
					}
				}
			}
			finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			}
		//	log.error("got " + list.size());
		}
		catch (SQLException se) {
			log.error("Encountered an error trying to fetch art entity " + se.getMessage());
			log.error("Query was: " + query);
		}
		log.debug("Query Processing ms: " + (Calendar.getInstance().getTimeInMillis() - start));
		//return null;
		return list;
	}
*/
	

	

}
