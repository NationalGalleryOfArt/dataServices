package gov.nga.integration.cspace;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( { 	
	"namespace", "source", "id", "type", "timespan"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeSpanContainer extends LinkedArtBaseClass {

	public TimeSpanContainer(String predicate, String namespace, String baseID, String source, TimeSpan timespan)  {
		super(namespace);
		setSource(source);
		setId(baseID + "/" + predicate);
		setTimespan(timespan);
	}
	
	private TimeSpan timespan = null;
	private void setTimespan(TimeSpan timespan) {
		this.timespan = timespan;
	}
	
	public TimeSpan getTimespan() {
		return timespan;
	}
	
}

