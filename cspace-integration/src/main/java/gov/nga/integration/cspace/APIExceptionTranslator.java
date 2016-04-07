package gov.nga.integration.cspace;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import gov.nga.entities.art.DataNotReadyException;

@ControllerAdvice
public class APIExceptionTranslator {

	private static final Logger log = LoggerFactory.getLogger(APIExceptionTranslator.class);
	
	@ExceptionHandler(APIUsageException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	ErrorLoggerResponse handleAPIUsageException(Exception e, HttpServletRequest req){
		log.warn(e.getMessage(),e);
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"There was a problem with the request. " + GenericErrorController.HELPLINK, 
				e.getMessage()
		);
	}

	@ExceptionHandler(DataNotReadyException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	ErrorLoggerResponse handleDataNotReadyException(DataNotReadyException e, HttpServletRequest req){
		log.warn(e.getMessage(),e);
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"The data service is still starting up and not yet ready to handle requests.", 
				e.getMessage()
		);
	}

	@ExceptionHandler(TypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	ErrorLoggerResponse handleTypeMismatchException(TypeMismatchException e, HttpServletRequest req){
		return handleAPIUsageException(e, req);
	}

/*	@ExceptionHandler({UsernameNotFoundException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	SimpleErrorMessage handleException(UsernameNotFoundException exception){
		log.debug("Username not found {}",exception.getLocalizedMessage());
		log.trace(exception.getMessage(),exception);
		return new SimpleErrorMessage("Unaouthorized"," ");
	}
*/

}

