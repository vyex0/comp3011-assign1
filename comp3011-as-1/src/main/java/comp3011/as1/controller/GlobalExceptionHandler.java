package comp3011.as1.controller;

import comp3011.as1.dto.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;

// Applies to every Controller for the application so that it returns the
// same error response shape required by the YAML file.
@RestControllerAdvice
public class GlobalExceptionHandler {
	// Calls the following function whenever an error occurs: ex is the actual exception
	// that occurs, we use the ex.getMessage() to get the error message. And request is
	// used to give us information about the HTTP request that caused the error, and we
	// use it to get the PATH.
	@ExceptionHandler(Exception.class)
	public  ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		// Builds the response in the form of ErrorResponse specified in the YAML.
		ErrorResponse error = new ErrorResponse(
			Instant.now().toString(),
			status.value(),
			status.getReasonPhrase(),
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		
		// Returns HTTP status INTERNAL SERVER ERROR: 500.
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error);
	}
	
}
