package gov.nga.entities.art;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ArtObjectExhibition extends ArtObjectTextEntry {
	
	public ArtObjectExhibition(ArtDataManagerService manager) {
		super(manager);
	}
	
	public ArtObjectExhibition(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		super(manager,rs);
	}
	
	public String getDescription() {
		return getText();
	}

}
