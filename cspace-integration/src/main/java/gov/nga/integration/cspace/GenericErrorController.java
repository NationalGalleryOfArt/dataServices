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


//@RestController
public class GenericErrorController { //implements org.springframework.boot.web.servlet.error.ErrorController {

//    private static final String PATH = "/error";
    // TODO - update this URL to point to github
    public static final String HELPLINK = 
    		"Please refer to the <a href=\"/apis/cspace.docx\">interface control document</a> for this service.";

 /*   @RequestMapping(value = PATH,method={RequestMethod.GET,RequestMethod.HEAD,RequestMethod.POST})
    public String error() {
        return "An error occurred trying to map your request to a service end point. " + HELPLINK;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
    
 */
}

/*** spring example ***/

/*@ControllerAdvice
public class myExceptionHandler extends ResponseEntityExceptionHandler {

 @ExceptionHandler(Exception.class)
 public final ResponseEntity<YourResponseClass> handleAllExceptions(Exception ex, WebRequest request) {
   YourResponseClassexceptionResponse = new YourResponseClass(new Date(), ex.getMessage());// Its an example you can define a class with your own structure
   return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
 }

 @ExceptionHandler(CustomException.class)
 public final ResponseEntity<YourResponseClass> handleAllExceptions(Exception ex, WebRequest request) {
   YourResponseClass exceptionResponse = new YourResponseClass(new Date(), ex.getMessage()); // For reference 
   return new ResponseEntity<YourResponseClass>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
 }

  @ExceptionHandler(BadCredentialsException.class)
 public final ResponseEntity<YourResponseClass> handleBadCredentialsException(BadCredentialsException ex, WebRequest request){
       YourResponseClass exceptionResponse = new YourResponseClass(new Date(), ex.getMessage());// For refernece 
           return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);          
 }  
*/