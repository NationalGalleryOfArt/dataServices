package gov.nga.integration.cspace;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( { 	
	"namespace", "source", "id", "type", "label", "begin_of_the_begin", "end_of_the_end"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeSpan extends LinkedArtBaseClass {

	private static final String defaultNamespace = "TimeSpan";

	public TimeSpan(Date begin, Date end, String label)  {
		super(defaultNamespace);
		setBegin_of_the_begin(begin);
		setEnd_of_the_end(end);
		setLabel(label);
	}
	
	public TimeSpan(int begin, int end, String label)  {
		this(new Long(begin), new Long(end), label);
	}
	
	public TimeSpan(Long begin, Long end, String label)  {
		super(defaultNamespace);
		Calendar c = Calendar.getInstance();
		if (begin != null) {
			c.set(begin.intValue(), 0, 1);
			setBegin_of_the_begin(c.getTime());
		}
		if (end != null) {
			c.set(end.intValue(), 11, 31 );
			setEnd_of_the_end(c.getTime());
		}
		setLabel(label);
	}
	
	public TimeSpan(Long begin, Long end)  {
		this(begin, end, null);
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private Date begin_of_the_begin = null;
	private void setBegin_of_the_begin(Date begin_of_the_begin) {
		this.begin_of_the_begin = begin_of_the_begin;
	}
	public String getBegin_of_the_begin() {
		if (begin_of_the_begin != null)
			return sdf.format(begin_of_the_begin);
		return null;
	}

	private Date end_of_the_end = null;
	private void setEnd_of_the_end(Date end_of_the_end) {
		this.end_of_the_end = end_of_the_end;
	}
	public String getEnd_of_the_end() {
		if (end_of_the_end != null)
			return sdf.format(end_of_the_end);
		else
			return null;
	}

}

