/*
    NGA ART DATA API: LinkedArtName extends the classified type and sets 
    its Type as "Name" thus representing the Linked Art Entity "Name"

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
package gov.nga.integration.cspace;

public class LinkedArtName extends LinkedArtClassifiedType {
	
	public LinkedArtName(String value, LinkedArtClassifiedType... classifications) {
		super("Name", null, value, classifications);
	}

}
