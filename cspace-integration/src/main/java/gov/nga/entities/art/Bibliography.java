/*
    NGA Art Data API: A bibliography record

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

import gov.nga.utils.TypeUtils;

import java.util.Comparator;

public interface Bibliography  {
	
	public static Comparator<Bibliography> sortByYearPublishedAsc = new Comparator<Bibliography>() {
		public int compare(Bibliography a, Bibliography b) {
			return TypeUtils.compare(a.getYearPublished(), b.getYearPublished());
		}
	};

	public Long getYearPublished();
	public String getCitation();

}
