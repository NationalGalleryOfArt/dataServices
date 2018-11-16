/*
    Utils: TypeUtils provides utilities for dealing with a variety of commonly used types
  
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

package gov.nga.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class TypeUtils {
	public static Boolean longToBoolean(Long l) {
		return Boolean.valueOf((l != null && l.longValue() == 1));
	}
	
	public static Long getLong(ResultSet rs, int columnIndex) throws SQLException {
		Long l = rs.getLong(columnIndex);
		return rs.wasNull() ? null : l;
	}

	public static Double getDouble(ResultSet rs, int columnIndex) throws SQLException {
		Double d = rs.getDouble(columnIndex);
		return rs.wasNull() ? null : d;
	}

	public static Calendar dateToCalendar(Date d) {
        if (d == null)
            return null;
		Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
	}
	
	public static <T extends Comparable<T>> int compare(T a, T b) {
		if (a == null && b == null)
			return 0;
		if (a == null)
			return 1;
		if (b == null)
			return -1;
		return a.compareTo(b);
	}
	
}
