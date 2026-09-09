package comp3011.as1.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.as1.dto.ErrorResponse;
import comp3011.as1.dto.GlobalStatsResponse;
import comp3011.as1.dto.ShutdownResponse;
import comp3011.as1.dto.UptimeResponse;
import comp3011.as1.service.ServerClockService;
import comp3011.as1.service.ShutdownStateService;
import comp3011.as1.service.TokenStatsService;

@RestController
@RequestMapping("/api")
public class AdminController {
	private final ConfigurableApplicationContext context; // Context 
	
	// Services
	private final TokenStatsService tokenStatsService;
	private final ShutdownStateService shutdownStateService;
	private final ServerClockService serverClockService;
	
	public AdminController (ServerClockService serverClockService, ConfigurableApplicationContext context, 
						    TokenStatsService tokenStatsService, ShutdownStateService shutdownStateService) {
		this.serverClockService = serverClockService;
		this.context = context;
		this.tokenStatsService = tokenStatsService;
		this.shutdownStateService = shutdownStateService;
	}
	
	// GET methods for each of the specified PATHS from YAML
	@GetMapping("/v1/admin/uptime")	
	public ResponseEntity<UptimeResponse> getUptime() {
		Instant start = serverClockService.getStartTime();
		Instant utcNow = Instant.now();
		double seconds = Duration.between(start, utcNow).toMillis() / 1000.0;
		
		UptimeResponse response = new UptimeResponse(
				start.toString(),
				utcNow.toString(),
				seconds
		);

		// 200: standard success response which matches the YAML specs
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/v1/global/stats")
	public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
		GlobalStatsResponse response = new GlobalStatsResponse(
				tokenStatsService.getInputTokens(),
				tokenStatsService.getOutputTokens()
		);
		
		// returns "200"
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(response);
	}
	
	// POST methods for specified PATHS from YAML
	@PostMapping("/v1/admin/shutdown")
	public ResponseEntity<?> shutdown() {
		
		// Rejects a second/concurrent shutdown request with 409 per
		// the YAML spec.
		if (!shutdownStateService.tryBeginShutdown()) {
			ErrorResponse error = new ErrorResponse(
					Instant.now().toString(),
					409,
					"Conflict",
					"Graceful shutdown is already in progress.",
					"/api/v1/admin/shutdown"
					);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		};
		
		ShutdownResponse response = new ShutdownResponse("Graceful shutdown requested.");

		// Context is closed on a separate thread after a short delay because
		// the process could terminate before the method's own HTTP response has
		// finished being sent back to the user.
		new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			context.close();
		}).start();
		
		// 202: request is accepted and shutdown process is in progress
		// but not yet completed which matches the YAML's specs.
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}
}
