package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.time.Instant;

// Stores the server start time and returns it for any
// function that calls for it (e.g., uptime endpoint).
@Service
public class ServerClockService {
	// Instant is used to represent the specific moment in time, and 
	// final prevents reassignment of the time.
	private final Instant startTime = Instant.now();
	
	// Get Methods
	public Instant getStartTime() {
		return startTime;
	};
	
	public Instant getUtcNow() {
		return Instant.now();
	};
}
