/*
    NGA Art Data API: MessageProvider is a simple provider interface that can be used
    for passing messages around in running code within the same JVM.  It is not intended
    to be a replacement or alternative for a robust persistent messaging solution like JMS.   

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

public interface MessageProvider {
	
	public enum EVENTTYPES {
		DATAREFRESHED;
	}
	
	public void subscribe(MessageSubscriber subscriber);
	
}
