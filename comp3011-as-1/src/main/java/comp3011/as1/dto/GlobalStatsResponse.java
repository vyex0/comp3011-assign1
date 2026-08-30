package comp3011.as1.dto;

public record GlobalStatsResponse (
	long inputTokens,
	long outputTokens
) {}