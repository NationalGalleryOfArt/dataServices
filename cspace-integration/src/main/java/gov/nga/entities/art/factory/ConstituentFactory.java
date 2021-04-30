/*
    NGA Art Data API: ConstituentFactory is a factory class for constituents
    and is involved in implementing a pattern by which subclasses of constituents
    that contain information only available in certain systems, e.g. the public
    web site, can still extent and leverage the searching, sorting, and other 
    management benefits of the base entity classes.  I'm not a huge fan of this
    pattern (there's a Java language limitation as root cause for needing it) 
    but we haven't found a pattern that works better yet. 

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
package gov.nga.entities.art.factory;

import gov.nga.common.entities.art.Constituent;

public interface ConstituentFactory <T>
{
	public T createConstituent(Constituent object);
}
