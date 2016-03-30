package gov.nga.integration.cspace;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nga.entities.art.ArtDataManagerService;

@RestController
public class CSpaceObjectRestAPI {

	private static final Logger log = LoggerFactory.getLogger(CSpaceObjectRestAPI.class);
	
    private static final String template = "Hello2, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @Autowired
    private ArtDataManagerService artDataManager;

    @RequestMapping("/greeting")
    public CSpaceSearchResponse greeting(@RequestParam(value="name", defaultValue="World") String name) {
    	log.info("***************************** " + artDataManager.getConfig());
        return new CSpaceSearchResponse(counter.incrementAndGet(), String.format(template, name + ": " + artDataManager.getConfig().getString("imagingServerURL")));
    }
    
}