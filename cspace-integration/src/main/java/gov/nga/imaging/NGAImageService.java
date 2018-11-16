// TODO move class to AEM packages
/*
    NGA AEM WEB API: NGAImageService is an interface returning images from
    a source - it should actually be relocated to the AEM system since it's not
    used by the NGA Art API code base  

    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: NGA Contractors

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
package gov.nga.imaging;


import java.sql.SQLException;
import java.util.List;


public interface NGAImageService
{
    List<NGAImage> getImages(Imagery.PROJECT project, Imagery.ENTITY_TYPE objectType, String objectId, Imagery.DISPLAYTYPE displayType) throws SQLException;
    
    NGAImage getImage(Imagery.PROJECT project, Imagery.ENTITY_TYPE objectType, String objectId, Imagery.DISPLAYTYPE displayType, String sequence) throws SQLException;
    
}
