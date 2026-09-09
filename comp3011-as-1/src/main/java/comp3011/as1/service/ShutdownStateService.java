package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ShutdownStateService {
	
	// Starts false and flips to true once the shutdown request is accepted,
	// AtomicBoolean so concurrent shutdown requests doesn't happen.
	private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
	
	// Returns true for the ONE caller that calls the shutdown.
	public boolean tryBeginShutdown() {
		return shutdownInProgress.compareAndSet(false, true);
	};
}
