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

	@MockitoBean
    private SttService sttService;
	
	// Testing a successful transcription, response should be OK once
	// it's all completed.
	@Test
	void transcribeReturnsTextAndUsage() throws Exception {
		// What the fake API would return when transcribe is called from SttService.
		TranscriptionResponse fakeResponse = new TranscriptionResponse(
				"I'm using the API",
				new TranscriptionResponse.Usage(10, 5)
		);
		when(sttService.transcribe(org.mockito.ArgumentMatchers.any()))
			.thenReturn(fakeResponse);
		
		// Fake audio file used to simulate the user's browser
		// uploading an audio file.
		MockMultipartFile audioFile = new MockMultipartFile(
				"audio",
				"test.webm",
				"audio/webm",                   // content-type
				"fake audio bytes".getBytes()
		);
		
		// Simulates POST request
		mockMvc.perform(multipart("/stt").file(audioFile))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.text").value("I'm using the API"))
			.andExpect(jsonPath("$.usage.input_tokens").value(10))
			.andExpect(jsonPath("$.usage.output_tokens").value(5));
	};
	
}
