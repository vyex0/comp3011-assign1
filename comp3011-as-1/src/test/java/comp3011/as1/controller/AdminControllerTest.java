package comp3011.as1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    
    @Test
    void uptimeReturns500WhenServerClockFails() throws Exception {
    	when(serverClockService.getStartTime())
    		.thenThrow(new RuntimeException("Uptime: Simulated clock failure"));
    	
    	mockMvc.perform(get("/api/v1/admin/uptime"))
    		.andExpect(status().isInternalServerError())
    		.andExpect(jsonPath("$.status").value(500))
    		.andExpect(jsonPath("$.error").value("Internal Server Error"));
    };
}
