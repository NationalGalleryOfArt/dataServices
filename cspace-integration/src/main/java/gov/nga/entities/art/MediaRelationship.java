/*
    NGA Art Data API: Location is a class that represents a place where art
    is displayed.  It is not necessarily trying to represent the same concept as
    the place a person was born or an art object created although I can imagine
    it evolving in that direction as we integrate controlled vocabularies more
    thoroughly in our data sets.   

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

public class MediaRelationship extends SupplementingEntityImpl {
	
	protected static final String fetchAllMediaRelationshipsQuery = 
			"SELECT mt.mediaID, mt.relatedEntity, mt.relatedID " +
			"FROM data.media_relationships mt";
	
	protected MediaRelationship(ArtDataManagerService manager) {
		super(manager,null);
	}
	
	public MediaRelationship(ArtDataManagerService manager, ResultSet rs) throws SQLException {
		this(manager);
		
		setMediaID(rs.getLong(1));
		
		// set the entity ID based on the type of associated entity - otherwise it's unsupported
		if ( rs.getString(2).equals(ArtObject.getEntityKeyStatic()) )
			setArtObjectID(rs.getLong(3));
		else if ( rs.getString(2).equals(Constituent.getEntityKeyStatic()) )
			setConstituentID(rs.getLong(3));
		else
			setEntityID(-1L);
	}
	
	public MediaRelationship factory(ResultSet rs) throws SQLException {
		MediaRelationship at = new MediaRelationship(getManager(),rs);
		return at;
	}
	
	private Long mediaID = null;
	private void setMediaID(Long mediaID) {
		this.mediaID = mediaID;
	}
	public Long getMediaID() {
		return this.mediaID;
	}

}