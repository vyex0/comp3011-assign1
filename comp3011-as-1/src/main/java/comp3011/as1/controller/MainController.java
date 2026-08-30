package comp3011.as1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
@Controller
public class MainController {
	
	// GET method for Index
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
}
