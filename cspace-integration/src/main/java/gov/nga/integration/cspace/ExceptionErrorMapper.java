package gov.nga.integration.cspace;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
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
public class ExceptionErrorMapper {

	private static final Logger log = LoggerFactory.getLogger(ExceptionErrorMapper.class);
	
	@ExceptionHandler(APIUsageException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorLoggerResponse handleAPIUsageException(Exception e, HttpServletRequest req){
		log.warn(e.getMessage(),e);
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"There was a problem with the request that prevented it from being processed at all. " + GenericErrorController.HELPLINK, 
				e.getMessage()
		);
	}

	@ExceptionHandler(SQLException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorLoggerResponse handleSQLException(SQLException e, HttpServletRequest req){
		log.warn(e.getMessage(),e);
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"A Database Exception was thrown when processing the request. ",
				"Details have been logged and will be inspected by an administrator of this system."
		);
	}

	@ExceptionHandler(ExecutionException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorLoggerResponse handleExecutionException(ExecutionException e, HttpServletRequest req){
		log.warn(e.getMessage(),e);
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"An Execution Exception was thrown when processing the request. ", 
				"Details have been logged and will be inspected by an administrator of this system."
		);
	}

	@ExceptionHandler(IOException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorLoggerResponse handleIOException(IOException e, HttpServletRequest req){
		String mesg = e.getMessage();
		// don't log errors when the client resets the connection - this happens in performance tests and pollutes the logs
		if (!mesg.equals("Connection reset by peer") && !mesg.equals("An established connection was aborted by the software in your host machine") ) {
			log.warn(e.getMessage(),e);
		}
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"An IO Exception was thrown when processing the request. ", 
				"Details have been logged and will be inspected by an administrator of this system."
		);
	}
	
	@ExceptionHandler(DataNotReadyException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorLoggerResponse handleDataNotReadyException(DataNotReadyException e, HttpServletRequest req){
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
	public ErrorLoggerResponse handleTypeMismatchException(TypeMismatchException e, HttpServletRequest req){
		return handleAPIUsageException(e, req);
	}
	
	@ExceptionHandler(ServletException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorLoggerResponse handleServletExceptionConflict(ServletException e, HttpServletRequest req) {
		return new ErrorLoggerResponse(
				"error", req.getRequestURI(), 
				"The data service is still starting up and not yet ready to handle requests.", 
				e.getMessage()
		);
    }

}

