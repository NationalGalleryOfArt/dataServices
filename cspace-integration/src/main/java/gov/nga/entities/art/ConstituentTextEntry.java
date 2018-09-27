/*
    NGA Art Data API: Text Entries for Constituents  

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
