package gov.nga.entities.art;

import java.sql.ResultSet;
import java.sql.SQLException;

/********************************************************************
 * @author d-beaudet
 * This class has been consumed largely by the ArtObjectTextEntry class
 * and is now just a wrapper class provided for clarity and backwards
 * compatibility
 ********************************************************************/

public class ArtObjectBibliography extends ArtObjectTextEntry implements Bibliography {

	public ArtObjectBibliography(ArtDataManagerService manager) {
		super(manager);
	}

	public ArtObjectBibliography(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,rs);
	}

	public String getCitation() {
		return getText();
	}

	public Long getYearPublished() {
		String year = getYear();
		if (year != null)
			return Long.valueOf(year);
		else
			return Long.valueOf(0);
	}

}
