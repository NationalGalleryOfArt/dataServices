/*
    Utils: ConcurrentUtils provides utilities for threads
  
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

package gov.nga.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentUtils {

	private static final Logger log = LoggerFactory.getLogger(ConcurrentUtils.class);

	// wait for another thread to notify on the given object
	public static void waitFor(Object o) {
		synchronized (o) {
			try {
				o.wait();
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}

}
