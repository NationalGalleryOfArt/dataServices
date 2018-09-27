/*
    NGA Art Entity Commons: Keyed value interface provides a standard
    interface for retrieving (usually database) keys from art entities.
    We should probably be specific that this is a database key as there
    could be other keys in use that might vary from one system to the 
    other depending on the implementation, for example, a URI might be
    the desired key in a linked data representation.  

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
package gov.nga.entities.common;

public interface KeyedValue {
	
	public String getKeyValue();

}
