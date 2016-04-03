package gov.nga.integration.cspace;

public class Record {
	AbridgedRecord record=null;
	
	public Record(AbridgedRecord record) {
		this.record=record;
	}
	
	public AbridgedRecord getRecord() {
		return this.record;
	}
}
