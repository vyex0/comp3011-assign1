package comp3011.as1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import java.time.Instant;

@SpringBootApplication
public class ServingWebApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ServingWebApplication.class, args);
	}
	
	@Bean
	Instant serverStartTime() {
		return Instant.now();
	};
}
