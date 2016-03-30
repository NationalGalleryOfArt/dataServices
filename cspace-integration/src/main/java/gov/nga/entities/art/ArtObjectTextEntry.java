package gov.nga.entities.art;

import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtObjectTextEntry extends TextEntry {
	
	private static final Logger log = LoggerFactory.getLogger(ArtObjectTextEntry.class);

	public ArtObjectTextEntry(ArtDataManagerService manager) {
		super(manager);
	}
	
	protected static final String allTextEntryQuery = 
		"SELECT t.fingerprint, t.textType, t.text, t.year, t.objectID " +
		"FROM data.objects_text_entries t " +
		"ORDER BY t.objectID, t.textType, t.year, t.text";
	
	public ArtObjectTextEntry(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,rs);
		objectID 	= TypeUtils.getLong(rs, 5);
	}
	
	public ArtObjectTextEntry factory(ResultSet rs) throws SQLException {
		String textType = rs.getString("textType");
		try {
			TEXT_ENTRY_TYPE te = TEXT_ENTRY_TYPE.textTypeForLabel(textType);
			switch (te) {
			case BIBLIOGRAPHY : 
				return new ArtObjectBibliography(getManager(), rs);
			case EXHIBITION_HISTORY : 
				return new ArtObjectExhibition(getManager(), rs); 
			default: 
				return new ArtObjectTextEntry(getManager(),rs);
			}
		}
		catch (NullPointerException ne) {
			log.error("Encountered unexpected label for text entry type " + textType);
			return new ArtObjectTextEntry(getManager(),rs);
		}
	}
	
	private Long objectID;
	public Long getObjectID() {
		return objectID;
	}
	
}
