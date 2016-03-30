package gov.nga.entities.art;

import gov.nga.utils.CollectionUtils;
import gov.nga.utils.TypeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static gov.nga.utils.StringUtils.htmlToMarkdown;
import static gov.nga.utils.StringUtils.sanitizeHtml;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public abstract class TextEntry extends ArtEntityImpl {
	
	// private static final Logger log = LoggerFactory.getLogger(TextEntry.class);
	
	public static enum TEXT_ENTRY_TYPE {
		BRIEF_NARRATIVE, CONSERVATION_NOTE, EXHIBITION_HISTORY, 
		EXHIBITION_HISTORY_FOOTNOTE, SYSTEMATIC_CATALOGUE, BIBLIOGRAPHY, UNKNOWN;
		
		static Map<String, TEXT_ENTRY_TYPE> myMap = CollectionUtils.newHashMap();
		
		static {
			myMap.put("brief_narrative",BRIEF_NARRATIVE);
			myMap.put("conservation_note",CONSERVATION_NOTE);
			myMap.put("exhibition_history",EXHIBITION_HISTORY);
			myMap.put("exhibition_history_footnote",EXHIBITION_HISTORY_FOOTNOTE);
			myMap.put("systematic_catalogue",SYSTEMATIC_CATALOGUE);
			myMap.put("bibliography",BIBLIOGRAPHY);
		}
		
		public static TEXT_ENTRY_TYPE textTypeForLabel(String label) {
			if (label != null) {
				TEXT_ENTRY_TYPE lookup = myMap.get(label);
				if (lookup != null)
					return lookup;
			}	
			return UNKNOWN;
		}
	}

	public TextEntry(ArtDataManagerService manager) {
		super(manager);
	}

	protected TextEntry(ArtDataManagerService manager, Long fingerprint) {
		super(manager,fingerprint);
	}

	public TextEntry(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,TypeUtils.getLong(rs, 1));
		textType 	= TEXT_ENTRY_TYPE.textTypeForLabel(rs.getString(2));
		text 		= htmlToMarkdown(sanitizeHtml(rs.getString(3)));
		year 		= rs.getString(4);
	}

	public static Comparator<TextEntry> sortByYearDesc = new Comparator<TextEntry>() {
		public int compare(TextEntry a, TextEntry b) {
			return TypeUtils.compare(b.getYear(), a.getYear());
		}
	};
	
	public static String textOf(TextEntry entry) {
		if (entry != null)
			return entry.getText();
		return null;
	}
	
	public static <T extends TextEntry> TextEntry firstTextEntryOfType(List<T> list, TEXT_ENTRY_TYPE dType) {
		List<T> newList = filterByTextType(list, dType);
		for (TextEntry te : newList)
			return te;
		return null;
	}

	public static <T extends TextEntry> String firstTextOfType(List<T> list, TEXT_ENTRY_TYPE dType) {
		return textOf(firstTextEntryOfType(list, dType));
	}

	
	public static <T extends TextEntry> List<T> filterByTextType(List<T> list, TEXT_ENTRY_TYPE dType) {
		List<T> newList = CollectionUtils.newArrayList();
		if (list != null) {
			if (dType == null)
				dType = TEXT_ENTRY_TYPE.UNKNOWN;
			for (T h : list) {
				if ( h.getTextType() == dType)
					newList.add(h);
			}
		}
		return newList;
	}

	private String text;
	public String getText() {
		return text;
	}
    public void setText(String text){
        this.text = text;
    }

    private TEXT_ENTRY_TYPE textType;
	public TEXT_ENTRY_TYPE getTextType() {
		return textType;
	}

	private String year;
	public String getYear() {
		return year;
	}

}
