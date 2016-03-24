package gov.nga.integration.cspace;

import javax.annotation.PostConstruct;

public class ArtDataManager {

	private String propValue=null;
	
	public ArtDataManager() {
		
	}
	
	public String getProperty(String name) {
		return propValue;
	}
	
    @PostConstruct
    public void init() {
        System.out.println(" ***************************** Initializing ArtDataManager *********************************");
        propValue=" I am a property!!! ";
    }
	
}
