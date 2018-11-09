package gov.nga.integration.cspace;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "url", "thumbnail", "record" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResultItem {
	Record record = null;
	URL	url = null;
	String thumbnail = null;
	
	public SearchResultItem(String thumbnail, Record record) {
		setRecord(record);
		setThumbnail(thumbnail);
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public URL getUrl() {
		return record.getUrl();
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
}
