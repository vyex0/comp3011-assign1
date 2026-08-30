package comp3011.as1.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import comp3011.as1.dto.TranscriptionResponse;

@Configuration
public class SttService {
	
	// Initialize the OpenAI's API
	@Bean
	RestClient restClient () {
		return RestClient.builder()
				.baseUrl("https://api.openai.com")
				.defaultHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
				.build();
	}
	
	// GET response from OpenAI to check connection
//	@Bean
//	CommandLineRunner pingOpenAi (RestClient restClient) {
//		return args -> {
//			String response = restClient.get()
//					.uri("/v1/models")
//					.retrieve()
//					.body(String.class);
//			
//			System.out.println("OpenAI response:" + response);
//		};
//	}
	
	public TranscriptionResponse transcribe (MultipartFile audio) {
		return new TranscriptionResponse("String");
	}
}
