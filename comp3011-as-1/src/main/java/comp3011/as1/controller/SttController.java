package comp3011.as1.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.SttService;

@RestController
public class SttController {
	
	// Delegates the OpenAI call to SttService
	private final SttService sttService;
	
	public SttController (SttService sttService) {
		this.sttService = sttService;
	}
	
	// Receives the recorded audio file from the browser and return its transcription
	// and its OpenAI token usage. 
	@PostMapping("/stt")	
	public TranscriptionResponse transcribe(
			@RequestParam("audio") MultipartFile audio) throws IOException {
		return this.sttService.transcribe(audio);
	}
}
