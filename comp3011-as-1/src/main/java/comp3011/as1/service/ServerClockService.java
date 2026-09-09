package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.time.Instant;

// Wraps the server's start time so it can be mocked in tests
// since mere Instant bean cannot be stubbed to simulate
// a failure for the regression tests.
@Service
public class ServerClockService {
	
	private final Instant startTime = Instant.now();
	
	public Instant getStartTime() {
		return startTime;
	};
}
