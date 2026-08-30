package comp3011.as1.dto;

import java.util.Objects;
import java.time.*;

public class UptimeResponse {
	private final String utcServerStart;
	private final String utcNow;
	private final double serverUptimeSeconds;
	
	public UptimeResponse(Instant startInstant) {
		Instant utcNow = Instant.now();
		double seconds = Duration.between(startInstant, utcNow).toMillis() / 1000.0;
		
		this.utcServerStart = startInstant.toString();
		this.utcNow = utcNow.toString();
		this.serverUptimeSeconds = seconds;
	}
	
	public String getUtcServerStart() { return utcServerStart; }
    public String getUtcNow() { return utcNow; }
    public double getServerUptimeSeconds() { return serverUptimeSeconds; }
}
