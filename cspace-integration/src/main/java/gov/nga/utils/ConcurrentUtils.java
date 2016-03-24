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
