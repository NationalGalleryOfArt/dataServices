package gov.nga.entities.art.datamanager.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nga.common.utils.SystemUtils;

public class GCNotifier implements Runnable 
{
    private static final Logger LOG = LoggerFactory.getLogger(GCNotifier.class);
	
	public void run ()
	{
		System.gc();
        LOG.info(String.format("Notifying Garbage Collector\n%s", SystemUtils.freeMemorySummary()));
	}
}
