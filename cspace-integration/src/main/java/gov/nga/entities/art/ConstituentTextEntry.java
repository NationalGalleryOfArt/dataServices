package gov.nga.entities.art;

import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstituentTextEntry extends TextEntry {
	
	private static final Logger log = LoggerFactory.getLogger(ConstituentTextEntry.class);

	public ConstituentTextEntry(ArtDataManagerService manager) {
		super(manager);
	}
	
	protected static final String allTextEntryQuery = 
		"SELECT t.fingerprint, t.textType, t.text, t.year, t.constituentID " +
		"FROM data.constituents_text_entries t " +
		"ORDER BY t.constituentID, t.textType, t.year, t.text";
	
	public ConstituentTextEntry(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,rs);
		constituentID 	= TypeUtils.getLong(rs, 5);
	}
	
	public ConstituentTextEntry factory(ResultSet rs) throws SQLException {
		String textType = rs.getString("textType");
		try {
			TEXT_ENTRY_TYPE te = TEXT_ENTRY_TYPE.textTypeForLabel(textType);
			switch (te) {
			case BIBLIOGRAPHY : 
				return new ConstituentBibliography(getManager(), rs); 
			default: 
				return new ConstituentTextEntry(getManager(),rs);
			}
		}
		catch (NullPointerException ne) {
			log.error("Encountered unexpected label for text entry type " + textType);
			return new ConstituentTextEntry(getManager(),rs);
		}
	}
	
	private Long constituentID;
	public Long getConstituentID() {
		return constituentID;
	}
	
}
