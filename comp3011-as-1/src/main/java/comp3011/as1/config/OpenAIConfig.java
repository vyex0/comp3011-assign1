package comp3011.as1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// Creates a RestClient and store it in the Spring application context, so
// it can be injected to the other services (e.g., sttService).
@Configuration
public class OpenAIConfig {
	@Bean
	RestClient restClient () {
		// Builds the RestClient: sets the baseURL, getting the API key from the 
		// .env and ensures each request has an authorization header.
		return RestClient.builder()
				.baseUrl("https://api.openai.com")
				.defaultHeader(
						"Authorization", 
						"Bearer " + System.getenv("OPENAI_API_KEY")
				)
				.build();
	}
}
