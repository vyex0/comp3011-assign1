package comp3011.as1.controller;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MainController.class)
public class MainControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private Instant serverStartTime; // mock Bean for test
	
	@Test
	void uptimeReturns500WhenServerClockFails() throws Exception {
		mockMvc.perform(get("/api/v1/admin/uptime"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.error").value("Internal Server Error"));
	}
}
