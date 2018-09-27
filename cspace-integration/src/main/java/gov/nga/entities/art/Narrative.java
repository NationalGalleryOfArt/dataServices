/*
    NGA Art Data API: Narrative is a deprecated class that has been replaced by
    TextEntry.  It will be removed in the next release of the NGA Art Data APIs. 

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

/********************************************************************
 * @author d-beaudet
 * This class has been consumed largely by the ArtObjectTextEntry class
 * and is now just a wrapper class provided for clarity and backwards
 * compatibility
 ********************************************************************/
@Deprecated 
public class Narrative extends ArtObjectTextEntry {
	
	public Narrative(ArtDataManagerService manager) {
		super(manager);
	}
	
	public String getNarrativeText() {
		return getText();
	}

	public TEXT_ENTRY_TYPE getNarrativeType() {
		return getTextType();
	}
}
	
/*	protected static final String allBriefOverviewQuery = 
		"SELECT n.fingerprint, n.textEntryID, n.objectID, " + 
		"       n.narrativeType, n.narrativeText " +
		"FROM data.objects_narratives n " +
		"WHERE n.narrativeType = 'brief_narrative' " +
		"ORDER BY n.textEntryID";
	
//	protected static final String baseBriefOverviewQuery =
//		allBriefOverviewQuery + " WHERE n.objectID @@  LIMIT 1";


	protected static final String allConservationNoteQuery = 
		"SELECT n.fingerprint, n.textEntryID, n.objectID, " + 
		"       n.narrativeType, n.narrativeText " +
		"FROM data.objects_narratives n " +
		"WHERE n.narrativeType = 'conservation_note' " +
		"ORDER BY n.textEntryID";
	
//	protected static final String baseConservationNoteQuery =
//		allConservationNoteQuery + " LIMIT 1";

	protected static final String allSysCatQuery = 
		"SELECT n.fingerprint, n.textEntryID, n.objectID, " + 
		"       n.narrativeType, n.narrativeText " +
		"FROM data.objects_narratives n " +
		"WHERE n.narrativeType = 'systematic_catalogue' " +
		"ORDER BY n.textEntryID";

//	protected static final String baseSysCatQuery =
//		allSysCatQuery + " LIMIT 1";

	public Narrative(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,	  TypeUtils.getLong(rs, 1));
		textEntryID 	= TypeUtils.getLong(rs, 2);
		objectID 		= TypeUtils.getLong(rs, 3);
		narrativeType 	= rs.getString(4);
		narrativeText 	= rs.getString(5);
	}
	
	public Narrative factory(ResultSet rs) throws SQLException {
		Narrative e = new Narrative(getManager(),rs);
		return e;
	}
*/
