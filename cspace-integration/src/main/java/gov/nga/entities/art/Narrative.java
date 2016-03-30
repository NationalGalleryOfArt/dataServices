package gov.nga.entities.art;

/********************************************************************
 * @author d-beaudet
 * This class has been consumed largely by the ArtObjectTextEntry class
 * and is now just a wrapper class provided for clarity and backwards
 * compatibility
 ********************************************************************/

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
