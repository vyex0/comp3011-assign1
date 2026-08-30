package comp3011.as1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAIConfig {
	
	// Initialize the OpenAI's API
	@Bean
	RestClient restClient () {
		return RestClient.builder()
				.baseUrl("https://api.openai.com")
				.defaultHeader(
						"Authorization", 
						"Bearer " + System.getenv("OPENAI_API_KEY")
				)
				.build();
	}
}
