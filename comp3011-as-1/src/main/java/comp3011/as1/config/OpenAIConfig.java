package comp3011.as1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAIConfig {
	
	// Initializes the OpenAI API client used for transcription endpoint.
	@Bean
	RestClient restClient () {
		return RestClient.builder()
				.baseUrl("https://api.openai.com")
				
				// Reads OPENAI_API_KEY environment variable once at startup and attaches
				// it as a bearer token to every request.
				.defaultHeader(
						"Authorization", 
						"Bearer " + System.getenv("OPENAI_API_KEY")
				)
				.build();
	}
}
