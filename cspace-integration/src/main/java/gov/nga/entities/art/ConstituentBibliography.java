package gov.nga.entities.art;

import gov.nga.utils.LongUtils;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConstituentBibliography extends ConstituentTextEntry implements Bibliography {
		
		// private static final Logger log = LoggerFactory.getLogger(ArtObjectBibliography.class);
		
		public ConstituentBibliography(ArtDataManagerService manager) {
			super(manager);
		}
		
		public ConstituentBibliography(ArtDataManagerService manager, ResultSet rs) throws SQLException {
			super(manager,rs);
		}
		
		public ConstituentBibliography factory(ResultSet rs) throws SQLException {
			ConstituentBibliography e = new ConstituentBibliography(getManager(),rs);
			return e;
		}
		
		public String getCitation() {
			return getText();
		}
		
		public Long getYearPublished() {
			return LongUtils.stringToLong(getYear(), Long.valueOf(0));
		}
		
}
