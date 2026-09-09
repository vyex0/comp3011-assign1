package comp3011.as1.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TokenStatsService {
	
	// Keeps track of the Tokens while Server is running 
	private final AtomicLong inputTokens = new AtomicLong(0);
	private final AtomicLong outputTokens = new AtomicLong(0);
	
	// Addition methods
	public void addInputTokens(long amount) {
		inputTokens.addAndGet(amount);
	}
	
	public void addOutputTokens(long amount) {
		outputTokens.addAndGet(amount);
	}
	
	// GET methods
	public long getInputTokens() {
		return inputTokens.get();
	}
	
	public long getOutputTokens() {
		return outputTokens.get();
	}
}
