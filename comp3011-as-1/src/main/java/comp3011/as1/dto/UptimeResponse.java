package comp3011.as1.dto;

public record UptimeResponse (
		String utcServerStart,
		String utcNow,
		double serverUptimeSeconds
) {}