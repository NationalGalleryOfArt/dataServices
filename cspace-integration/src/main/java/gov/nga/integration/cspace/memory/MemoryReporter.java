package gov.nga.integration.cspace.memory;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gov.nga.common.utils.SystemUtils;

@Service
public class MemoryReporter {

	private static final Logger log = LoggerFactory.getLogger(MemoryReporter.class);

    @PostConstruct
    public void postConstruct() {
    	
        log.info("******************************************* MemoryReporter Started **********************************************");
       
        
    }
    
	@Scheduled(cron="0 0/15 * * * ?")
	public void reportMemory() {
		log.info(SystemUtils.freeMemorySummary());
	}
}
