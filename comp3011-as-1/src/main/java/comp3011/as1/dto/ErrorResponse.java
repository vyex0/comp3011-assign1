package comp3011.as1.dto;

public record ErrorResponse (
		String timestamp,
		int status,
		String error,
		String message,
		String path
) {}
