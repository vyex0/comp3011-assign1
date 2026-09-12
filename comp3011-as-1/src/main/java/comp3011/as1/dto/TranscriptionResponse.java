package comp3011.as1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranscriptionResponse (
		String text,
		Usage usage
) {
	public record Usage (
		@JsonProperty("input_tokens")
		long inputTokens,
		
		@JsonProperty("output_tokens")
		long outputTokens
	) {}
}
