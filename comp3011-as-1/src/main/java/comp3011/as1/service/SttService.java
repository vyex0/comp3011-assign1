package comp3011.as1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;

import comp3011.as1.dto.TranscriptionResponse;

@Service
public class SttService {
	
	private final RestClient restClient;
	
	public SttService (RestClient restClient) {
		this.restClient = restClient;
	};
	
	// Receives the Multipart Audio and transcribe it using the OpenAI model
	public TranscriptionResponse transcribe (MultipartFile audio) throws IOException {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("model", "gpt-4o-mini-transcribe"); // Uses the gpt-4o-mini-transcribe model
		
		String filename = audio.getOriginalFilename();
		byte[] audioBytes = audio.getBytes();
		ByteArrayResource audioResource = new ByteArrayResource(audioBytes);
		
		builder.part("file", audioResource)
			   .filename(filename);
		
		TranscriptionResponse response = restClient.post()
				.uri("/v1/audio/transcriptions")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(builder.build())
				.retrieve()
				.body(TranscriptionResponse.class);
		
		return response;
	}
}
