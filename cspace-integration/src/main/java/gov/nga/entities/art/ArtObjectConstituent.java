/*
    NGA Art Data API: Art Entity for the relationship between a Constituent 
    and an art object. 

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

import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ArtObjectConstituent extends ArtEntityImpl {
	
	private Constituent constituent;
	
	// private static final Logger log = LoggerFactory.getLogger(ArtObjectConstituent.class);
	
	public static final String RELATEDARTISTROLE = "related artist";
	public static final String AFTERWORKSROLE    = "artist after";
	public static final String ARTISTROLETYPE    = "artist";
	public static final String OWNERROLETYPE     = "owner";
	public static final String CURRENTOWNERROLE	 = "current owner";
	public static final String PREVIOUSOWNERROLE = "previous owner";
	
	protected static final String fetchAllObjectsConstituentsQuery = 
		"SELECT oc.fingerprint, oc.constituentID, oc.objectID, " + 
		"		oc.role, oc.roleType, oc.displayOrder, " + 
		"		oc.priorOwnerInvNum " + 
		"FROM data.objects_constituents oc ";
		// we might want to exclude the NGA (ID==12) as a constituent because we're 
		// listed as current owner of every
		// object in our collection (with only a handful of exceptions)
		// WHERE oc.constituentID <> 12";

	protected static final String fetchFirstObjectConstituentQuery = fetchAllObjectsConstituentsQuery + " LIMIT 1";
	
	public ArtObjectConstituent(ArtDataManagerService manager) {
		super(manager);
	}

	public ArtObjectConstituent(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		// order of query is as defined above
		super(manager, 	  	  TypeUtils.getLong(rs, 1));
		constitutendID 		= TypeUtils.getLong(rs, 2);
		objectID 			= TypeUtils.getLong(rs, 3);
		role				= rs.getString(4);
		roleType			= rs.getString(5);
		displayOrder		= TypeUtils.getLong(rs, 6);
		priorOwnerInvNum 	= rs.getString(7);
	}
	
	public ArtObjectConstituent factory(ResultSet rs) throws SQLException {
		ArtObjectConstituent aoc = new ArtObjectConstituent(getManager(), rs);
		return aoc;
	}
	
	public static Comparator<ArtObjectConstituent> sortByDisplayOrderAsc = new Comparator<ArtObjectConstituent>() {
		public int compare(ArtObjectConstituent a, ArtObjectConstituent b) {
			int ocompare = a.getObjectID().compareTo(b.getObjectID());
			int dcompare = a.getDisplayOrder().compareTo(b.getDisplayOrder());
			return ocompare == 0 ? dcompare : ocompare;
		}
	};

	public String getKeyValue() {
		return getObjectID() + "; " + getConstituentID() + "; " + getDisplayOrder() + "; " + getRoleType() + "; " + getRole();
	}
	
	private void loadConstituent() {
		constituent = getManager().fetchByConstituentID(this.getConstituentID());
	}
	
	public Constituent getConstituent() {
		// return Constituent.fetchByConstituentID(this.getConstituentID());
		if (constituent == null)
			loadConstituent();
		return constituent;
	}

	private Long constitutendID 	= null;
	public Long getConstituentID() {
		return constitutendID;
	}

	private Long objectID 			= null;
	public Long getObjectID() {
		return objectID;
	}
	
	private Long displayOrder 		= null;
	public Long getDisplayOrder() {
		return displayOrder;
	}

	private String role				= null;
	public String getRole() {
		return role;
	}

	private String roleType			= null;
	public String getRoleType() {
		return roleType;
	}

	private String priorOwnerInvNum = null;
	public String getPriorOwnerIvnNum() {
		return priorOwnerInvNum;
	}

}