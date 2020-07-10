package gov.nga.integration.records.suggestion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nga.entities.art.ArtDataSuggestion;
import gov.nga.integration.records.ResultItem;

@JsonPropertyOrder({ "suggestion", "entityid" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestResultItem extends ResultItem {
	
	String suggestion;
	Long entityid;
	
	public SuggestResultItem(final ArtDataSuggestion suggestion) {
		this.suggestion = suggestion.getDisplayString();
		entityid = suggestion.getEntityID();
	}
	
	public String getSuggestion() {
		return suggestion;
	}
	
	public Long getEntityid() {
		return entityid;
	}
}
