/*
    Utils: DateUtils provides utilities for parsing, fetching, and manipulating Dates
  
    Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: David Beaudet, NGA Contractors

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	public static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
	public static final String DATE_FORMAT_MMMDDYYYY = "MMM dd, yyyy";
	public static final String DATE_FORMAT_MMMDYYYY = "MMM d, yyyy";
	public static final String DATE_FORMAT_MMMMDYYYY = "MMMM d, yyyy";
	public static final String DATE_FORMAT_MMMMD  = "MMMM d";
	public static final String DATE_FORMAT_YYYY = "yyyy";
	public static final String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd";
	@Deprecated
	public static final String DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DATE_FORMAT_ISO_8601_WITH_TIME_AND_TZ_CORRECT = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class); 

	public static Calendar parseJSONDate( String input ) throws ParseException {

		if (input == null || input.equals(""))
			return null;
		//      log.info("input:\t\t" + input);

		SimpleDateFormat df = new SimpleDateFormat( "y-M-d'T'h:m:s.SSSz" );

		//this is zero time so we need to add that TZ indicator for
		// convert Zulu to an rfc compliant timezone
		if ( input.toLowerCase().endsWith( "z" ) ) {
			input = input.substring( 0, input.length() - 1) + "-0000";
		} 

		Date d = df.parse(  input );
		Calendar c = null;
		if (d != null) {
			c = Calendar.getInstance();
			c.setTime(d);
		}
		return c;
	}

	
	public static String convertDate(String fromFormat, String toFormat, String dateStr) {
		try {   
			logger.debug("!%Date is converted from : " + fromFormat +  " format to " + toFormat + " date: " + dateStr);

			if (dateStr == null) 
				return new String();  

			SimpleDateFormat sdfSource = new SimpleDateFormat(fromFormat);

			Date date = sdfSource.parse(dateStr);

			SimpleDateFormat sdfDestination = new SimpleDateFormat(toFormat);

			dateStr = sdfDestination.format(date);

			logger.debug("!Date is converted from : " + fromFormat +  " format to " + toFormat);
			logger.debug("!Converted date is : " + dateStr);

		} catch(ParseException pe) {
			logger.error("Parse Exception : " + pe);
		}
		return dateStr;
	}

	public static String formatDate(String toFormat, Date date) {
		if (date == null || toFormat == null) 
			return null;  
		SimpleDateFormat sdfDestination = new SimpleDateFormat(toFormat);
		return sdfDestination.format(date);
	}

	/*
	 * This method returns dates span with both years if start and end year are different and
	 * with one year, when start and end year are the same.
	 * Example: January 23 – March 15, 2013 (when both dates are in the same year) 
	 * or October 19, 2013 – February 4, 2014 
	 */

	public static String returnDateSpan(String startDate, String endDate) {
		StringBuffer res = new StringBuffer();

		logger.debug("Date is converted start : " + startDate +  "  end " + endDate);

		String startYear = convertDate(DATE_FORMAT_MMDDYYYY, DATE_FORMAT_YYYY, startDate);
		String endYear = convertDate(DATE_FORMAT_MMDDYYYY, DATE_FORMAT_YYYY, endDate);
		logger.debug("Start-end years: " + startYear + " " + endYear);

		if (startYear.equalsIgnoreCase(endYear)) {
			res.append(convertDate(DATE_FORMAT_MMDDYYYY, DATE_FORMAT_MMMMD, startDate));
		} else {
			res.append(convertDate(DATE_FORMAT_MMDDYYYY, DATE_FORMAT_MMMMDYYYY, startDate));
		}

		res.append("&nbsp;&ndash;&nbsp;");
		res.append(convertDate(DATE_FORMAT_MMDDYYYY, DATE_FORMAT_MMMMDYYYY, endDate));
		logger.debug("1Converted date is : " + res.toString());
		return res.toString();
	}

	public static Calendar today() {
		Calendar c = Calendar.getInstance();
		c.set( 
			c.get(Calendar.YEAR), 
			c.get(Calendar.MONTH), 
			c.get(Calendar.DAY_OF_MONTH),
			0,0,0);
        c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	public static Date todayDate() {
		return today().getTime();
	}

	public static Calendar tomorrow() {
		Calendar c = today();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c;
    }
	
	public static Date tomorrowDate() {
		return tomorrow().getTime();
	}

	public static Calendar yesterday() {
		Calendar c = today();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return c;
    }
	
	public static Date yesterdayDate() {
		return yesterday().getTime();
	}

	private static Calendar compareCalendar(Calendar a, Calendar b, boolean getGreater) {
		if (a == null && b == null)
			return null;
		if (a == null)
			return b;
		if (b == null)
			return a;
		return getGreater ? 
			( a.getTime().compareTo(b.getTime()) >= 0 ? a : b ) :
			( a.getTime().compareTo(b.getTime()) <  0 ? a : b );
	}

	public static Calendar latest(Calendar a, Calendar b) {
		return compareCalendar(a,b,true);
	}

	public static Calendar earliest(Calendar a, Calendar b) {
		return compareCalendar(a,b,false);
	}

 }