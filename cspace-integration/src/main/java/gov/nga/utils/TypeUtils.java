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
