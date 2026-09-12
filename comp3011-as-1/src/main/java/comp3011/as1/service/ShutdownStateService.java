package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicBoolean;

// Shutdown service to ensure that only a single shutdown request
// is accepted.
@Service
public class ShutdownStateService {
	
	// AtomicBoolean is used to ensure that only a single shutdown
	// request is accepted in case there's concurrent shutdown requests.
	private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
	
	// Returns TRUE for the one caller that requests the shutdown.
	public boolean tryBeginShutdown() {
		return shutdownInProgress.compareAndSet(false, true);
	};
}
