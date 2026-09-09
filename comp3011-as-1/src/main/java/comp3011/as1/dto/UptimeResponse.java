package comp3011.as1.dto;

import java.time.*;

public record UptimeResponse (
		String utcServerStart,
		String utcNow,
		double serverUptimeSeconds
) {}