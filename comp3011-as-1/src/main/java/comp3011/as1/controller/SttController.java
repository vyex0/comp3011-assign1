package comp3011.as1.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.SttService;

public class SttController {
	
	private final SttService sttService;
	
	public SttController (SttService sttService) {
		this.sttService = sttService;
	}
	
	// POST method for using the OpenAI transcribe service
	@PostMapping
	public TranscriptionResponse transcribe(@RequestParam("audio") MultipartFile audio) {
		return this.sttService.transcribe(audio);
	}
}
