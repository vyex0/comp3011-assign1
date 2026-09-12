package comp3011.as1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.SttService;

@WebMvcTest(SttController.class)
public class SttControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

	// Replaces the SttService with a Mock
	@MockitoBean
    private SttService sttService;
	
	// Testing SttController and ensuring that SttController returns the correct
	// response when SttService returns a response.
	@Test
	void transcribeReturnsTextAndUsage() throws Exception {
		// Ensures the fakeResponse is sent whenever 
		// SttService.transcribe() is called
		TranscriptionResponse fakeResponse = new TranscriptionResponse(
				"I'm using the API",
				new TranscriptionResponse.Usage(10, 5)
		);
		when(sttService.transcribe(org.mockito.ArgumentMatchers.any()))
			.thenReturn(fakeResponse);
		
		// Fake uploaded audio file.
		MockMultipartFile fakeAudio = new MockMultipartFile(
				"audio", "test.webm", "audio/webm", "fake audio bytes".getBytes()
			);
		
		// Simulates POST to my /stt endpoint with the fakeAudio file sent.
		// Ensures the correct status code, text, and tokens statistics are correct.
		mockMvc.perform(multipart("/stt").file(fakeAudio))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.text").value("I'm using the API"))
			.andExpect(jsonPath("$.usage.input_tokens").value(10))
			.andExpect(jsonPath("$.usage.output_tokens").value(5));
	};
	
}
