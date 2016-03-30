package gov.nga.entities.art;

import gov.nga.search.SortHelper;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ConstituentAltName extends ArtEntityImpl {
	
	// private static final Logger log = LoggerFactory.getLogger(ConstituentAltName.class);
	
	public ConstituentAltName(ArtDataManagerService manager) {
		super(manager);
	}

/*	protected static final String fetchAllConstituentAltNamessQuery =
		"SELECT a.fingerprint, a.altNameID, a.constituentID, " + 
		"       a.lastName, a.displayName, a.nameType " +
		"FROM data.constituents_altnames a ";
*/
	
	protected static final String fetchAllConstituentAltNamessQuery =
		"SELECT a.fingerprint, a.altNameID, a.constituentID, " + 
		"       a.lastName, a.displayName, a.nameType " +
		"FROM data.constituents_altnames a " +
		"WHERE nameType NOT IN ('Preferred Name', 'Variant Index Name') ";

	protected static final String baseAltNamesQuery = 
		fetchAllConstituentAltNamessQuery + " AND a.constituentID @@ ";
	
	public ConstituentAltName(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,	  TypeUtils.getLong(rs, 1));
		altNameID 		= TypeUtils.getLong(rs, 2);
		constituentID 	= TypeUtils.getLong(rs, 3);
		lastName 		= rs.getString(4);
		displayName 	= rs.getString(5);
		nameType 		= rs.getString(6);
	}
	
	public ConstituentAltName factory(ResultSet rs) throws SQLException {
		ConstituentAltName a = new ConstituentAltName(getManager(),rs);
		return a;
	}
	
	public static Comparator<ConstituentAltName> sortByDisplayNameAsc = new Comparator<ConstituentAltName>() {
		public int compare(ConstituentAltName a, ConstituentAltName b) {
			Integer c = SortHelper.compareObjects(a.getDisplayName(), b.getDisplayName());
			if (c == null)
				return 0;
			return c;
		}
	};
	
	private Long altNameID = null;
	public long getAltNameID() {
		return altNameID;
	}

	private Long constituentID = null;
	public long getConstituentID() {
		return constituentID;
	}

	private String displayName = null;
	public String getDisplayName() {
		return displayName;
	}

	private String nameType = null;
	public String getNameType() {
		return nameType;
	}

	private String lastName = null;
	public String getLastName() {
		return lastName;
	}
}
