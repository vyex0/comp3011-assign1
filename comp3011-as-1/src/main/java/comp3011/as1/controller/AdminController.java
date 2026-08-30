package comp3011.as1.controller;

import java.time.Instant;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.as1.dto.GlobalStatsResponse;
import comp3011.as1.dto.ShutdownResponse;
import comp3011.as1.dto.UptimeResponse;
import comp3011.as1.service.TokenStatsService;

@RestController
public class AdminController {
	private final ConfigurableApplicationContext context; // Context 
	private Instant serverStartTime = null; // Start Time of the Server
	
	private final TokenStatsService tokenStatsService;
	
	public AdminController (Instant serverStartTime, ConfigurableApplicationContext context, 
						    TokenStatsService tokenStatsService) {
		this.serverStartTime = serverStartTime;
		this.context = context;
		this.tokenStatsService = tokenStatsService; 
	}
	
	// GET methods for each of the specified PATHS from YAML
	@GetMapping("/api/v1/admin/uptime")
	public UptimeResponse getUptime() {
		return new UptimeResponse(serverStartTime);
	}
	
	@GetMapping("/api/v1/global/stats")
	public GlobalStatsResponse getGlobalStats() {
		return new GlobalStatsResponse(
				tokenStatsService.getInputTokens(),
				tokenStatsService.getOutputTokens()
		);
	}
	
	// POST methods for specified PATHS from YAML
	@PostMapping("/api/v1/admin/shutdown")
	public ResponseEntity<ShutdownResponse> shutdown() {
		ShutdownResponse response = new ShutdownResponse("Graceful shutdown requested.");

		// Thread is used to delay the shutdown process so that response can be delivered first
		// before the context is shut down.
		new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			context.close();
		}).start();
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}
}
