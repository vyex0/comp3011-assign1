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

// The controller responsible for the API routing calls to my server's end point.
// It receives admin requests and returns the appropriate HTTP responses.
@RestController
@RequestMapping("/api")
public class AdminController {
	private final ConfigurableApplicationContext context; // Spring application's running context 
	
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
	
	// Get Methods for each of the specified PATHS from the YAML doc.
	@GetMapping("/v1/admin/uptime")	
	public ResponseEntity<UptimeResponse> getUptime() {
		// Get the server start time using ServerClockService service, the utcTime,
		// and calculates the server uptime until the current second.
		Instant start = serverClockService.getStartTime();
		Instant utcNow = serverClockService.getUtcNow();
		double seconds = Duration.between(start, utcNow).toMillis() / 1000.0;
		
		// Builds the response in the form of UptimeResponse.
		UptimeResponse response = new UptimeResponse(
				start.toString(),
				utcNow.toString(),
				seconds
		);

		// Returns HTTP status OK: 200 if successful. 
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping("/v1/global/stats")
	public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
		// Calls the tokenStatsService to get the token statistics,
		// and builds the response in the form of GlobalStatsResponse.
		GlobalStatsResponse response = new GlobalStatsResponse(
				tokenStatsService.getInputTokens(),
				tokenStatsService.getOutputTokens()
		);
		
		// Returns HTTP status OK: 200 if successful. 
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	// POST Method for each of the specified PATHS from the YAML doc.
	@PostMapping("/v1/admin/shutdown")
	public ResponseEntity<?> shutdown() {
		// Checks whether the shutdown is already in progress, if shutdown
		// is already called then returns HTTP status CONFLICT: 409.
		if (!shutdownStateService.tryBeginShutdown()) {
			ErrorResponse error = new ErrorResponse(
					serverClockService.getUtcNow().toString(),
					409,
					"Conflict",
					"Graceful shutdown is already in progress.",
					"/api/v1/admin/shutdown"
					);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// Builds the response in the form of ShutdownResponse.
		ShutdownResponse response = new ShutdownResponse("Graceful shutdown requested.");

		
		// Context is closed on a separate thread after a short delay (500 ms) to ensure
		// the HTTP response is sent back to the user first before context is fully terminated.
		new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			context.close();
		}).start();
		
		// Returns HTTP status ACCEPTED: 202 if successful. 
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}
}
