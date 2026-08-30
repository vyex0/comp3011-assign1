package comp3011.as1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	// GET methods for Pages
	@GetMapping("/")
	public String index() {
		return "Index";
	}
}
