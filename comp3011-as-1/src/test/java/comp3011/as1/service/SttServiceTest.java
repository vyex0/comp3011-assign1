package comp3011.as1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

public class SttServiceTest {
	
	private RestClient restClient;
    private MockRestServiceServer mockServer;
    private TokenStatsService tokenStatsService;
    private SttService sttService;
    
    // Shared fake OpenAI API response across every test that needs a
    // successful transcription result.
    private static final String fakeJson = """
            {
                "text": "I'm using the API",
                "usage": {
                    "input_tokens": 10,
                    "output_tokens": 5
                }
            }
            """;
    
    // Shared fake uploaded audio file, reused across every test that needs it.
    private static final MultipartFile fakeAudio = new MockMultipartFile(
			"audio", "test.webm", "audio/webm", "fake audio bytes".getBytes()
		);
    
    // Mock server intercepts the request made by the RestClient so the request
    // never leaves the JVM. I control what the response would be like from the
    // mock OpenAI API. Ensured to be run before any tests.
    @BeforeEach
    void setup() {
    	RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        
        tokenStatsService = new TokenStatsService();
        
        sttService = new SttService(restClient, tokenStatsService);
    };
    
    // Tests the application's logging system, ensures that the logs
    // returned captures the correct input and output tokens.
    @Test
    void transcribeLogsSuccessWithTokenCounts() throws Exception {
    	// Obtains the logger SttService uses so it can listen to the
    	// logs produced by SttService.
    	Logger sttLogger = (Logger) LoggerFactory.getLogger(SttService.class);
    	
    	// Everytime there's a new log from SttService, the log is
    	// appended to the list
    	ListAppender<ILoggingEvent> appender = new ListAppender<>();
    	appender.start();
    	sttLogger.addAppender(appender);
    	
    	// Try-catch ensures the log collector detaches.
    	try {
    		// Ensures that the endpoint called returns the fakeJSON.
        	mockServer.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
        		.andExpect(method(HttpMethod.POST))
        		.andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));
        	
        	sttService.transcribe(fakeAudio);
        	
        	// appender contains all the logs that was captured and lets us process
        	// the request. We check whether the logs contain the input & output needed.
        	boolean loggedSuccess = appender.list.stream()
        			.anyMatch(event -> event.getFormattedMessage().contains("inputTokens=10")
        					&& event.getFormattedMessage().contains("outputTokens=5"));
        	assertTrue(loggedSuccess, "Expected a log entry recording token usage on success");
        		
        	// Ensures the mock server receives the request. It would fail
            // if SttService never called the RestClient.
            mockServer.verify();
    	} finally {
    		// Detaches this test's log collector so that it doesn't keep listening
        	// after testing is done.
        	sttLogger.detachAppender(appender);
    	}
    };
    
    
    // Tests the functional behavior of the application: request is built
    // correctly, response from the API gets parsed correctly, and the
    // input & output tokens in TokenStatsService gets updated correctly.
    @Test
    void transcribeSendsCorrectRequestAndUpdatesStats() throws Exception {
    	mockServer.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
    		.andExpect(method(HttpMethod.POST))
    		.andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));
    	
    	var response = sttService.transcribe(fakeAudio);
    	
    	// Assert 1: SttService parsed the API response correctly, ensuring
    	// both the text and the input & output tokens are correct.
        assertEquals("I'm using the API", response.text());
        assertEquals(10, response.usage().inputTokens());
        assertEquals(5, response.usage().outputTokens());
        
        // Assert 2: Ensures TokenStatsService receives the correct
        // amount of tokens and outputs the correct amount.
        assertEquals(10, tokenStatsService.getInputTokens());
        assertEquals(5, tokenStatsService.getOutputTokens());

        mockServer.verify();
    };
    
    // Tests the failure of the SttService when the OpenAI API is down,
    // ensuring exception is thrown and token statistics are not updated.
    @Test
    void transcribeDoesNotUpdateStatsWhenOpenAiCallFails() {
    	// Simulates OpenAI API being down and rejects the request,
    	// returning Internal Server Error.
    	mockServer.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
	        .andExpect(method(HttpMethod.POST))
	        .andRespond(withServerError());
    	
    	// Ensures the RestClient throws an exception error when
    	// the transcribe function is called.
    	assertThrows(
            org.springframework.web.client.RestClientException.class,
            () -> sttService.transcribe(fakeAudio)
        );
    	
    	// Because RestClient is never run, the token statistics
    	// shouldn't be updated.
    	assertEquals(0, tokenStatsService.getInputTokens());
        assertEquals(0, tokenStatsService.getOutputTokens());
        
        mockServer.verify();
    };
}
