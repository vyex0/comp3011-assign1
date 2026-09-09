package comp3011.as1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TokenStatsServiceConcurrencyTest {
	
	@Test
	void addInputTokensIsThreadSafeUnderConcurrentUpdates() throws Exception {
		TokenStatsService tokenStatsService = new TokenStatsService();
		
		int threadCount = 250;
		int incrementsPerThread = 100;
		long amountPerIncrement = 1;
		
		// A pool of threads running concurrently on TokenStatsService instance.
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		
		for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    tokenStatsService.addInputTokens(amountPerIncrement);
                }
            });
        }
		
		executor.shutdown();
		
		// Blocks until submitted tasks are finished or 30 seconds has passed,
		// 30 seconds is the safety net so build doesn't run forever.
		executor.awaitTermination(30, TimeUnit.SECONDS);
		
		// Mathematically exact expected total, if every single increment
		// was actually counted with none lost.
		long expectedTotal = threadCount * incrementsPerThread * amountPerIncrement;
		
		assertEquals(expectedTotal, tokenStatsService.getInputTokens(),
	            "Lost updates detected - counter is not thread-safe under concurrent access");
	};
}
