package gov.nga.entities.art;

public interface MessageProvider {
	
	public enum EVENTTYPES {
		DATAREFRESHED;
	}
	
	public void subscribe(MessageSubscriber subscriber);
	
}
