package comp3011.as1.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;

import comp3011.as1.dto.TranscriptionResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Informs Spring that this is a business logic and treats it as a service,
// so that it can be used by other classes.
@Service
public class SttService {
	// Used to send the HTTP request to the OpenAI API.
	private final RestClient restClient;
	
	// A logger created specifically for the API calls to OpenAI API, used
	// to keep track of the API calls made to the API.
	private static final Logger log = LoggerFactory.getLogger(SttService.class);
	
	// For /api/v1/global/stats token usage.
	private final TokenStatsService tokenStatsService;
	
	public SttService (RestClient restClient, TokenStatsService tokenStatsService) {
		this.restClient = restClient;
		this.tokenStatsService = tokenStatsService;
	};
	
	// Sends one complete recording to OpenAI's transcription API and records token usage.
	// Called once after the user has finished recording (in script.js).
	public TranscriptionResponse transcribe (MultipartFile audio) throws IOException {
		String filename = audio.getOriginalFilename();
		
		// In case the filename is NULL
		if (filename == null || filename.isBlank()) {
			filename = "recording.webm";
		};
		
		// Logs that the STT request was received and show the filename.
		log.info("STT request received: filename={}", filename);
		
		// OpenAI expects the audio request to be sent as multiform/form-data
		// and MultipartBodyBuilder helps in constructing that package of information.
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("model", "gpt-4o-mini-transcribe"); // Uses the gpt-4o-mini-transcribe model
		
		// Takes the uploaded audio and reads its content into memory as series of bytes,
		// then wraps the bytes in a resource so Spring treats it as a file.
		byte[] audioBytes = audio.getBytes();
		ByteArrayResource audioResource = new ByteArrayResource(audioBytes);
		
		// Adds the audio file and name to the HTTP request
		builder.part("file", audioResource)
			   .filename(filename);
		
		// Builds the HTTP post and sends it to the API, and converts the JSON response
		// to my application's TranscriptionResponse object.
		TranscriptionResponse response = restClient.post()
				.uri("/v1/audio/transcriptions")              // specified endpoint
				.contentType(MediaType.MULTIPART_FORM_DATA)   // content type of file
				.body(builder.build())                        // attached the body with the builded file
				.retrieve()
				.body(TranscriptionResponse.class);
		
		// Updates the global stats counter after a successful call.
		this.tokenStatsService.addInputTokens(response.usage().inputTokens());
		this.tokenStatsService.addOutputTokens(response.usage().outputTokens());
		
		// Logs that the STT response was retrieved and shows the input/output tokens.
		log.info("STT request succeeded: inputTokens={}, outputTokens={}",
				response.usage().inputTokens(), response.usage().outputTokens());
		
		return response;
	}
}
