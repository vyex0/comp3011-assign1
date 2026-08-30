package comp3011.as1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.as1.dto.UptimeResponse;

import java.time.Instant;

@RestController
public class MainController {
	
	// Server Start Time starts when it receives a HTTP Request
	private Instant serverStartTime = null;
	
	public MainController (Instant serverStartTime) {
		this.serverStartTime = serverStartTime;
	}
	
	// GET methods for each of the specified PATHS from YAML
	@GetMapping("/")
	public String index() {
		return "Index";
	}
	
	@GetMapping("/api/v1/admin/uptime")
	public UptimeResponse getUptime() {
		return new UptimeResponse(serverStartTime);
	}
	
	@GetMapping("/api/v1/admin/shutdown")
	public String getShutdown() {
		return "Admin/Shutdown";
	}
	
	@GetMapping("/api/v1/global/stats")
	public String getStats() {
		return "Global/Stats";
	}
	
}
