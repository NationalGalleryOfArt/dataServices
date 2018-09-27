/*
    NGA Art Data API: MessageProviderImpl is a simple implementation of
    the MessageProvider interface.

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
