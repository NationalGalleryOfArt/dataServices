/*
    NGA Art Data API: Art Entity for an Object's Bibliography Records 
    TODO - review whether it would make sense to convert this to
    a general purpose bibliography object rather than being specific
    to art objects 
    
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
