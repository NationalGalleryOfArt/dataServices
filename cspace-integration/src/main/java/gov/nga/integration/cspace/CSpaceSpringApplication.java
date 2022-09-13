package gov.nga.integration.cspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@EnableScheduling
@ComponentScan({"gov.nga.integration.cspace","gov.nga.api.iiif.auth"})
@ContextHierarchy({
    @ContextConfiguration("production-context.xml")
})
public class CSpaceSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CSpaceSpringApplication.class, args);
    }

}