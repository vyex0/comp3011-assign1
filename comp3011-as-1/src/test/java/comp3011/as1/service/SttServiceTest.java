package comp3011.as1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

public class SttServiceTest {
	
	private RestClient restClient;
    private MockRestServiceServer mockServer;
    private TokenStatsService tokenStatsService;
    private SttService sttService;
    
    @BeforeEach
    void setup() {
    	
    	// A real restClient but the server is bound to the mock server underneath,
    	// so the request never actually leave the JVM or hit a real network.
    	RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        
        tokenStatsService = new TokenStatsService();
        
        sttService = new SttService(restClient, tokenStatsService);
    };
    
    @Test
    void transcribeSendsCorrectRequestAndUpdatesStats() throws Exception {
    	String fakeJson = """
            {
                "text": "I'm using the API",
                "usage": {
                    "input_tokens": 10,
                    "output_tokens": 5
                }
            }
            """;
    	
    	mockServer.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
	        .andExpect(method(HttpMethod.POST))
	        .andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));
    	
    	// Fake uploaded audio file
    	MultipartFile audio = new MockMultipartFile(
            "audio", "test.webm", "audio/webm", "fake audio bytes".getBytes()
        );
    	
    	var response = sttService.transcribe(audio);
    	
    	// Assert 1: the method returned the parsed response correctly
        assertEquals("I'm using the API", response.text());
        assertEquals(10, response.usage().openAI_inputTokens());
        assertEquals(5, response.usage().openAI_outputTokens());
        
        // Assert 2: TokenStartsService real counters are incremented correctly
        assertEquals(10, tokenStatsService.getInputTokens());
        assertEquals(5, tokenStatsService.getOutputTokens());
        
        // Confirms the mock server receives the expected request at all,
        // would fail if SttService never called restClient at all.
        mockServer.verify();
    };
    
    @Test
    void transcribeDoesNotUpdateStatsWhenOpenAiCallFails() {
    	
    	// Simulates OpenAI being down and rejects the request,
    	// returning server error instead of valid transcription.
    	mockServer.expect(requestTo("https://api.openai.com/v1/audio/transcriptions"))
	        .andExpect(method(HttpMethod.POST))
	        .andRespond(withServerError());
    
    	MultipartFile audio = new MockMultipartFile(
            "audio", "test.webm", "audio/webm", "fake audio bytes".getBytes()
        );
    	
    	// RestClient throws an exception when it receives no response,
    	// this should throw and not return normally.
    	assertThrows(
            org.springframework.web.client.RestClientException.class,
            () -> sttService.transcribe(audio)
        );
    	
    	// Since thrown, input/output tokens never runs and
    	// should still be their initial value.
    	assertEquals(0, tokenStatsService.getInputTokens());
        assertEquals(0, tokenStatsService.getOutputTokens());
        
        mockServer.verify();
    };
}
