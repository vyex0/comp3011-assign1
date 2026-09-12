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
		
		// Sets the update: 500 threads, each thread does 100 updates,
		// each update increments the token count by 1.
		int threadCount = 500;
		int incrementsPerThread = 100;
		long amountPerIncrement = 1;
		
		long start = System.currentTimeMillis();
		
		// A pool of virtual threads mimicking 500 separate threads hitting the
		// application all at once. I use virtual threads because we can use
		// more thread counts and it is much cheaper compared to using platform threads.
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		
		// Performs the task 500x100 times.
		for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    tokenStatsService.addInputTokens(amountPerIncrement);
                }
            });
        }
		
		executor.shutdown();
		
		// Blocks here until the threads are all finished or when 10 seconds has passed.
		executor.awaitTermination(10, TimeUnit.SECONDS);
		
		long durationMs = System.currentTimeMillis() - start;
		
		// Expected total, if every single increment was actually counted and none are lost.
		// Ensures the application counted all the increments correctly.
		long expectedTotal = threadCount * incrementsPerThread * amountPerIncrement;
		assertEquals(expectedTotal, tokenStatsService.getInputTokens(),
				(tokenStatsService.getInputTokens() - expectedTotal) + " tokens are lost."
						+ " Request took " + durationMs + "ms");
	};
}
