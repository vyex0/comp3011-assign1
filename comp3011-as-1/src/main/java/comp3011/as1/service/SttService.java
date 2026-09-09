package comp3011.as1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;

import comp3011.as1.dto.TranscriptionResponse;

@Service
public class SttService {
	
	private final RestClient restClient;
	
	// For /api/v1/global/stats token usage.
	private final TokenStatsService tokenStatsService;
	
	public SttService (RestClient restClient, TokenStatsService tokenStatsService) {
		this.restClient = restClient;
		this.tokenStatsService = tokenStatsService;
	};
	
	// Sends one complete recording to OpenAI's transcription API and records token usage.
	// Called once after the user has finished recording (in script.js).
	public TranscriptionResponse transcribe (MultipartFile audio) throws IOException {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("model", "gpt-4o-mini-transcribe"); // Uses the gpt-4o-mini-transcribe model
		
		String filename = audio.getOriginalFilename();
		
		// MultipartFile's content is only available once after as a stream,
		// so it's buffered into memory and wrapped as a ByteArrayResource. 
		byte[] audioBytes = audio.getBytes();
		ByteArrayResource audioResource = new ByteArrayResource(audioBytes);
		
		// Builds the input into a file and a multipart request for OpenAI
		builder.part("file", audioResource)
			   .filename(filename);
		
		// Parses OpenAI's JSON response directly into my own TranscriptionResponse object
		TranscriptionResponse response = restClient.post()
				.uri("/v1/audio/transcriptions")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(builder.build())
				.retrieve()
				.body(TranscriptionResponse.class);
		
		// Updates the global stats counter after a successful call.
		this.tokenStatsService.addInputTokens(response.usage().openAI_inputTokens());
		this.tokenStatsService.addOutputTokens(response.usage().openAI_outputTokens());
		
		return response;
	}
}
