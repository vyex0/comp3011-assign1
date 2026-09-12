package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

// Shared token counter used to keep track of the number of input
// & output tokens that has been used while the application is running.
@Service
public class TokenStatsService {
	
	// Atomic long is used to ensure the token statistics are
	// correct even when there's concurrent requests happening.
	private final AtomicLong inputTokens = new AtomicLong(0);
	private final AtomicLong outputTokens = new AtomicLong(0);

	// Update Methods
	public void addInputTokens(long amount) {
		inputTokens.addAndGet(amount);
	}
	
	public void addOutputTokens(long amount) {
		outputTokens.addAndGet(amount);
	}
	
	// Get Methods
	public long getInputTokens() {
		return inputTokens.get();
	}
	
	public long getOutputTokens() {
		return outputTokens.get();
	}
}
