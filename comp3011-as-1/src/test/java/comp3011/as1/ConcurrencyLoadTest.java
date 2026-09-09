package comp3011.as1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcurrencyLoadTest {
	
	@LocalServerPort
    private int port;
	
	@Test
	void handles200ConcurrentUptimeRequests() throws Exception {
		TestRestTemplate restTemplate = new TestRestTemplate();
        String url = "http://localhost:" + port + "/api/v1/admin/uptime";
        
        int requestCount = 250;
        
        // An array of OS threads, mimicking 250 separate clients hitting
        // the server at once.
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        
        List<Callable<ResponseEntity<String>>> tasks = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
        	tasks.add(() -> restTemplate.getForEntity(url, String.class));
        };
        
        long start = System.currentTimeMillis();
        
        // Submits all tasks at once and blocks here until every single
        // one is completed. Concurrency happens here.
        List<Future<ResponseEntity<String>>> results = executor.invokeAll(tasks);
        
        long durationMs = System.currentTimeMillis() - start;
        executor.shutdown();
        
        // Assert 1: every single request should succeed.
        for (Future<ResponseEntity<String>> result : results) {
        	assertEquals(200, result.get().getStatusCode().value());
        };
        
        // Assert 2: 250 threads should complete below this set time.
        assertTrue(durationMs < 10_000,
        		"250 concurrent requests took " + durationMs + "ms - too slow");
	}
}
