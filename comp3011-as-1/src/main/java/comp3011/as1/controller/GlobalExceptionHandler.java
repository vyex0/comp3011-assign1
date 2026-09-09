package comp3011.as1.controller;

import comp3011.as1.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;

// Applies to every controller for the app so it returns the same ErrorResponse
// shape required by the YAML file.
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// Handles specific failure: client request missing a required
	// field (e.g., /stt called with no audio file uploaded).
	
	@ExceptionHandler(Exception.class)
	public  ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		ErrorResponse error = new ErrorResponse(
			Instant.now().toString(),
			status.value(),
			status.getReasonPhrase(),
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		
		// returns "500"
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error);
	}
	
}
