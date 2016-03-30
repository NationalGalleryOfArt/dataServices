package gov.nga.integration.cspace;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenericErrorController implements ErrorController{

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "An error occurred trying to map your request to a service end point.  Please check the interface specifications";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
