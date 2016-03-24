package gov.nga.integration.cspace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SingletonTest {
    @Bean // should default to singleton
    public ArtDataManager artDataManager() {
    	return new ArtDataManager();
    }
}