// TODO - eliminate this class through refactoring
/*
    NGA ART DATA API: TimeSpanContainer is a JSON bean for representing TimeSpan under a node 
    actually called "timespan".  The calling classes can probably be refactored to eliminate this class
    as similar functionality has been achieved in other ways by merely creating the appropriate getters
    on the classes containing timespans... 
  
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

