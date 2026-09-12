package comp3011.as1.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.as1.dto.TranscriptionResponse;
import comp3011.as1.service.SttService;

// Controller that sits between application and SttService, any
// transcription requests are delegated to the SttService.
@RestController
public class SttController {
	private final SttService sttService;
	
	public SttController (SttService sttService) {
		this.sttService = sttService;
	}
	
	// Call the following method when a POST request is made to
	// the /stt endpoint, with the audio file uploaded along.
	@PostMapping("/stt")	
	public TranscriptionResponse transcribe(
			@RequestParam("audio") MultipartFile audio) throws IOException {
		return this.sttService.transcribe(audio);
	}
}
