/*
    NGA Art Data API: Alternative Names for Constituents - a constituent is
    not always referred to by the same name, although there must be a primary
    one assigned.  This object is used to represent the other names by which
    a constituent might be known. 

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
		"       a.lastName, a.displayName, a.forwardDisplayName, a.nameType " +
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
		forwardDisplayName 	= rs.getString(6);
		nameType 		= rs.getString(7);
	}
	
	public ConstituentAltName factory(ResultSet rs) throws SQLException {
		ConstituentAltName a = new ConstituentAltName(getManager(),rs);
		return a;
	}
	
	public static Comparator<ConstituentAltName> sortByDisplayNameAsc = new Comparator<ConstituentAltName>() {
		public int compare(ConstituentAltName a, ConstituentAltName b) {
			return SortHelper.compareObjects(a.getDisplayName(), b.getDisplayName());
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

	private String forwardDisplayName = null;
	public String getForwardDisplayName() {
		return forwardDisplayName;
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
