package gov.nga.integration.cspace;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenericErrorController implements ErrorController {

    private static final String PATH = "/error";
    public static final String HELPLINK = 
    		"Please refer to the <a href=\"/apis/cspace.docx\">interface control document</a> for this service.";

    @RequestMapping(value = PATH)
    public String error() {
        return "An error occurred trying to map your request to a service end point. " + HELPLINK;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
    
 
}
