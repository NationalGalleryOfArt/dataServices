/*
    NGA Art Data API: Bibliography for a Constituent 
    TODO - review whether we actually need an artObject and consituent
    bibliography or whether one class might suffice - since this class
    implements "Bibliography" that might be sufficient for abstraction
    given the other constraints with how data is loaded but still worth
    thinking about.

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
