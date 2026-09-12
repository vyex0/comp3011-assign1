package comp3011.as1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import java.time.Instant;

// Starts the Spring Boot Application
@SpringBootApplication
public class ServingWebApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ServingWebApplication.class, args);
	}
	
	// Use the serverStartTime to record the exact time the
	// application starts running.
	@Bean
	Instant serverStartTime() {
		return Instant.now();
	};
}
