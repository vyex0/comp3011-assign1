package comp3011.as1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.SttService;

// Starts the spring boot application for the test because we're testing
// 200+ real concurrent HTTP requests to the running application.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcurrencyLoadTest {
	// Spring puts the application's port number into this variable
	@LocalServerPort
    private int port;
	
	// Mock SttService for testing purposes
	@MockitoBean
    private SttService sttService;
	
	// Mock action for sttService.transcribe(). It can receive any input
	// and outputs given response. Ensured to be run before any tests.
	@BeforeEach
	void setupMockDelay() throws Exception {
		when(sttService.transcribe(any())).thenAnswer(invocation -> {
			Thread.sleep(300);
			return new TranscriptionResponse(
					"fake transcription",
					new TranscriptionResponse.Usage(10, 5)
			);
		});
	};
	
	// This test is done to ensure that the application can handle more
	// than 250 concurrent HTTP request from various clients.
	@Test
	void handles200plusConcurrentSttRequests() throws Exception {
		// Spring utility to send actual HTTP requests to the running application
		TestRestTemplate restTemplate = new TestRestTemplate();
        String url = "http://localhost:" + port + "/stt";
        
        Callable<ResponseEntity<String>> task = () -> {
        	// MultipartFile body to be constructed.
        	LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        	
        	// Creates a fake file containing the filename 'test.webm' and add
        	// some fake bytes content, and attach it to the MultipartFile body.
        	ByteArrayResource fakeAudio = new ByteArrayResource("fake audio bytes".getBytes()) {
        		@Override public String getFilename() { return "test.webm"; }
        	};
        	body.add("audio", fakeAudio);
        	
        	// Combines the body + the headers as a complete HTTP request to send.
        	HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            return restTemplate.postForEntity(url, request, String.class);
        };
        
        // An collection of virtual threads rather than platform threads, mimicking 500 
        // separate clients hitting the server at once. I use virtual threads 
        // because we'll be waiting for a response from OpenAI's API than do some
        // heavy computing, and therefore virtual threads are best for tasks
        // with a lot of waiting time between threads.
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        int requestCount = 500;
        
        List<Callable<ResponseEntity<String>>> tasks = Collections.nCopies(requestCount, task);
        
        long start = System.currentTimeMillis();
        
        // Submits the HTTP request tasks all at once and blocks here until 
        // every single one is completed. 
        List<Future<ResponseEntity<String>>> results = executor.invokeAll(tasks);
        
        long durationMs = System.currentTimeMillis() - start;
        executor.shutdown();
        
        // Assert 1: ensures every single request task succeeds and returns
        // a status code 200.
        for (Future<ResponseEntity<String>> result : results) {
        	assertEquals(200, result.get().getStatusCode().value());
        };
        
        // Assert 2: ensures the application finishes all the task within the 
        // set time 3000 so that application quits whenever there's an error.
        // Synchronously should take 15000ms, but we'll try limiting to 1500ms instead.
        assertTrue(durationMs < 1500,
        		requestCount + "concurrent requests took " + durationMs + "ms");
	}
}
