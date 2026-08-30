package comp3011.as1.controller;

import comp3011.as1.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// Catches uncaught exceptions
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
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
