/*
    NGA ART DATA API: NameSpaceUtils provides a small set of simple static convenience functions for
    parsing or otherwise working with name spaces

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

import gov.nga.utils.StringUtils;

public class NamespaceUtils {
	
	public final static String nameSpaceDelimeter = ":";

	public static String getNamespace(String fieldName, String defaultNamespace) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(nameSpaceDelimeter);
		if (parts.length > 1)
			return parts[0];
		return defaultNamespace;
	}

	public static String stripNamespace(String fieldName) {
		if (fieldName == null)
			return null;
		String[] parts = fieldName.split(nameSpaceDelimeter);
		if (parts.length > 1)
			return parts[1];
		else
			return parts[0];
	}

	public static String ensureNamespace(String fieldName, String defaultNamespace) {
		String fn = stripNamespace(fieldName);
		if (StringUtils.isNullOrEmpty(fn))
			return null;
		String ns = getNamespace(fieldName,defaultNamespace);
		if (StringUtils.isNullOrEmpty(ns))
			return null;
		return ns + nameSpaceDelimeter + fn;
	}

}
