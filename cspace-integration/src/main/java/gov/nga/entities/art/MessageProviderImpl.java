package gov.nga.entities.art;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.utils.CollectionUtils;

public abstract class MessageProviderImpl implements MessageProvider {
	
	private static final Logger log = LoggerFactory.getLogger(MessageProviderImpl.class);
	
	List<MessageSubscriber> subscribers = CollectionUtils.newArrayList();
	
	public void subscribe(MessageSubscriber subscriber) {
		log.debug("Subscribing: " + subscriber.getClass().getName());
		if (!subscribers.contains(subscriber) )
			subscribers.add(subscriber);
	}
	
	public void unsubscribe(MessageSubscriber subscriber) {
		log.debug("Unsubscribing: " + subscriber.getClass().getName());
		if (!subscribers.contains(subscriber) )
			subscribers.remove(subscriber);
	}
	
	protected void sendMessage(EVENTTYPES event) {
		for (MessageSubscriber m : subscribers) {
			log.debug("Sending to: " + m.getClass().getName());
			m.receiveMessage(event);
		}
	}

}
