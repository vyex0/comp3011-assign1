package comp3011.as1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Practice
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class MainController {
	
	// GET methods for each of the specified PATHS from YAML
	@GetMapping("/")
	public String index() {
		return "Index";
	}
	
	@GetMapping("/api/v1/admin/uptime")
	public String getUptime() {
		return "Admin/Uptime";
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
