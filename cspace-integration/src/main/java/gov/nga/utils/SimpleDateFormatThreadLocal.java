package gov.nga.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatThreadLocal {

    // SimpleDateFormat is not thread-safe, so give one to each thread
	// parser is a static simpledateformat instance that is wrapped in threadlocal
	// so each thread gets it's own initial instance
	// the constructor simply gets that instance and sets the proper format for it
	// then the caller can execute parse() to perform the required operations
	
	private String defaultFormat = DateUtils.DATE_FORMAT_ISO_8601;
	
    // private static final ThreadLocal<SimpleDateFormat> parser = new ThreadLocal<SimpleDateFormat>(){
    private ThreadLocal<SimpleDateFormat> parser = new ThreadLocal<SimpleDateFormat>() {
    	
    	protected SimpleDateFormat initialValue() {
    		// default to a standard
            return new SimpleDateFormat(defaultFormat);
        }
    };
    
	private SimpleDateFormatThreadLocal(String format) {
		defaultFormat = format;
	}
	
	public static SimpleDateFormatThreadLocal getInstance(String format) {
		return new SimpleDateFormatThreadLocal(format);
	}

	public SimpleDateFormat getParser() {
		return parser.get();
	}
	
    public Date parse(String dateString) throws ParseException {
    	SimpleDateFormat sf = parser.get();
   		return sf.parse(dateString);
    }
    
    public String format(Date date) {
    	SimpleDateFormat sf = parser.get();
		return sf.format(date);
    }
    
}