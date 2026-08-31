package comp3011.as1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.TokenStatsService;

@Service
public class SttService {
	
	private final RestClient restClient;
	private final TokenStatsService tokenStatsService;
	
	public SttService (RestClient restClient, TokenStatsService tokenStatsService) {
		this.restClient = restClient;
		this.tokenStatsService = tokenStatsService;
	};
	
	// Receives the Multipart Audio and transcribe it using the OpenAI model
	public TranscriptionResponse transcribe (MultipartFile audio) throws IOException {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("model", "gpt-4o-mini-transcribe"); // Uses the gpt-4o-mini-transcribe model
		
		String filename = audio.getOriginalFilename();
		
		// Get binary content of audio file and turn it into ByteArrayResource
		byte[] audioBytes = audio.getBytes();
		ByteArrayResource audioResource = new ByteArrayResource(audioBytes);
		
		// Builds the input into a file and a multipart request for OpenAI
		builder.part("file", audioResource)
			   .filename(filename);
		
		TranscriptionResponse response = restClient.post()
				.uri("/v1/audio/transcriptions")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(builder.build())
				.retrieve()
				.body(TranscriptionResponse.class);
		
		this.tokenStatsService.addInputTokens(response.usage().openAI_inputTokens());
		this.tokenStatsService.addOutputTokens(response.usage().openAI_outputTokens());
		
		return response;
	}
}
