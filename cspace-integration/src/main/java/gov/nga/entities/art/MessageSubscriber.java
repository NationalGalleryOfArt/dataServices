package gov.nga.entities.art;

import gov.nga.entities.art.MessageProvider.EVENTTYPES;

public interface MessageSubscriber {
	
	public void receiveMessage(EVENTTYPES event);

}
