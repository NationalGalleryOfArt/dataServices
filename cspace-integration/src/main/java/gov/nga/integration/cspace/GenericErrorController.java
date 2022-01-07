/*
    NGA ART DATA API: GenericErrorController is the backstop for any otherwise unhandled exceptions

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
package gov.nga.integration.cspace;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenericErrorController implements ErrorController {

    private static final String PATH = "/error";
    // TODO - update this URL to point to github
    public static final String HELPLINK = 
    		"Please refer to the <a href=\"/apis/cspace.docx\">interface control document</a> for this service.";

    @RequestMapping(value = PATH,method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public String error() {
        return "An error occurred trying to map your request to a service end point. " + HELPLINK;
    }

    
    public String getErrorPath() {
        return PATH;
    }
    
 
}
