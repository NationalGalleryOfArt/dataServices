package gov.nga.integration.cspace;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "url", "record", "thumbnail" })
public class Item {
	Record record = null;
	URL	url = null;
	String thumbnail = null;
	
	public Item(URL url, String thumbnail, Record record) {
		setRecord(record);
		setUrl(url);
		setThumbnail(thumbnail);
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
}
