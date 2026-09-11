package comp3011.as1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import comp3011.as1.service.ServerClockService;
import comp3011.as1.service.ShutdownStateService;
import comp3011.as1.service.TokenStatsService;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerClockService serverClockService;
    
    @MockitoBean
    private TokenStatsService tokenStatsService;

    @MockitoBean
    private ShutdownStateService shutdownStateService;
    
    // Tests the success of the uptime endpoint, ensuring the endpoint
    // works in normal conditions and normal response 200 is returned.
    @Test
    void uptimeReturns200WithCorrectFields() throws Exception {
    	// Response from serverClockService is always fixed to
    	// 2026-09-01T00:00:00Z
    	Instant fixedStart = Instant.parse("2026-09-01T00:00:00Z");
    	when(serverClockService.getStartTime()).thenReturn(fixedStart);
    	
    	// Simulates a HTTP request through the AdminController so 
    	// serverClockService can be called and response is posted.
    	mockMvc.perform(get("/api/v1/admin/uptime"))
    		.andExpect(status().isOk())
    		.andExpect(jsonPath("$.utcServerStart").value(fixedStart.toString()))
    		.andExpect(jsonPath("$.serverUptimeSeconds").isNumber());
    };
    
    // Tests the failure of the uptime endpoint when the serverClockService
    // fails, and ensures the appropriate response 500 is returned.
    // ServerClockService is chosen to be failed because the main function of
    // the uptime endpoint revolves around the time of the server's uptime.
    @Test
    void uptimeReturns500WhenServerClockFails() throws Exception {
    	// Pretends the clock server has failed and throw a runtime exception.
    	when(serverClockService.getStartTime())
    		.thenThrow(new RuntimeException("Uptime: Simulated ServerClockService failure"));
    	
    	mockMvc.perform(get("/api/v1/admin/uptime"))
    		.andExpect(status().isInternalServerError())
    		.andExpect(jsonPath("$.status").value(500))
    		.andExpect(jsonPath("$.error").value("Internal Server Error"))
    		.andExpect(jsonPath("$.timestamp").exists())
    		.andExpect(jsonPath("$.path").value("/api/v1/admin/uptime"));
    };
    
    // Tests the success of the global stats endpoint, ensuring the endpoint
    // works in normal conditions and normal response 200 is returned and
    // the correct number of input & output tokens are returned.
    @Test
    void globalStatsReturns200WithTokenCounts() throws Exception {
    	// tokenStatsService are forced to return these fixed values.
    	when(tokenStatsService.getInputTokens()).thenReturn(18432L);
        when(tokenStatsService.getOutputTokens()).thenReturn(4096L);
        
        mockMvc.perform(get("/api/v1/global/stats"))
        	.andExpect(status().isOk())
        	.andExpect(jsonPath("$.inputTokens").value(18432))
        	.andExpect(jsonPath("$.outputTokens").value(4096));
    };
    
    // Tests the failure of the global stats endpoint when the tokenStatsService
    // fails, and ensures the appropriate response 500 is returned.
    // tokenStatsService is chosen to be failed because the main function of
    // the global stats endpoint revolves around ensuring the number of
    // input and output tokens from an API response is correct.
    @Test
    void globalStatsReturns500WhenServiceFails() throws Exception {
    	when(tokenStatsService.getInputTokens())
    		.thenThrow(new RuntimeException("Stats: Similated TokenStatsService failure"));
        
        mockMvc.perform(get("/api/v1/global/stats"))
        	.andExpect(status().isInternalServerError())
        	.andExpect(jsonPath("$.status").value(500))
    		.andExpect(jsonPath("$.error").value("Internal Server Error"))
    		.andExpect(jsonPath("$.timestamp").exists())
    		.andExpect(jsonPath("$.path").value("/api/v1/global/stats"));
    };
    
    // Tests the success of the shutdown endpoint, ensuring the endpoint
    // works in normal conditions and normal response 202 is returned.
    @Test
    void shutdownReturns202WhenNotAlreadyInProgress() throws Exception {
    	when(shutdownStateService.tryBeginShutdown()).thenReturn(true);
        
        mockMvc.perform(post("/api/v1/admin/shutdown"))
        	.andExpect(status().isAccepted())
        	.andExpect(jsonPath("$.message").value("Graceful shutdown requested."));
    };
    
    // Tests the failure of the shutdown endpoint when a shutdown process
    // has already been called. Ensuring the appropriate response 409 is returned.
    @Test
    void shutdownReturns409WhenAlreadyInProgress() throws Exception {
    	when(shutdownStateService.tryBeginShutdown()).thenReturn(false);
        
        mockMvc.perform(post("/api/v1/admin/shutdown"))
        	.andExpect(status().isConflict())
        	.andExpect(jsonPath("$.status").value(409))
    		.andExpect(jsonPath("$.error").value("Conflict"))
    		.andExpect(jsonPath("$.path").value("/api/v1/admin/shutdown"));
    };
}
