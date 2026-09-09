// -- Recording the Audio --
let stream;
let recorder;
let audioChunks = [];

async function startRecording () {
	// Prompts the user's browser permission dialog and returns a 
	// live audio stream from the user's microphone.
	stream = await navigator.mediaDevices.getUserMedia({
		audio:true
	});
	
	// mimeType is set explicitly so every audio chunk 
	// and the final blob is audio/webm.
	recorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
	audioChunks = [];
	
	// Fires repeatedly when recording is active, handing the audio
	// in small pieces rather than all at once
	recorder.ondataavailable = function(event) {
		audioChunks.push(event.data);
	};
	
	// Invoked once stopRecording is called.
	recorder.onstop = async function() {
		document.getElementById("status").textContent = "Processing transcription...";
		
		// Combines all audio chunks to one audio file and
		// sends it to my own backend STT endpoint.
		const audioBlob = new Blob(audioChunks, { type: "audio/webm" });
		
		const formData = new FormData();
		formData.append("audio", audioBlob, "userRecording.webm");
	
		const response = await fetch("/stt", {
			method: "POST",
			body: formData
		});
		
		const data = await response.json();
		
		// Displays the transcribed text and resets status, ready for
		// a new recording without page reload.
		document.getElementById("transcription").textContent = data.text;
		document.getElementById("status").textContent = "Not recording";
	};
	
	// Starts recording the user's audio.
	recorder.start();
	document.getElementById("status").textContent = "Recording...";
}

// -- Stopped Recording --
function stopRecording () {
	recorder.stop();
	
	// Releases the user's mic so the browser's recording
	// indicator turns off and mic isn't reserved anymore after use. 
	stream.getTracks().forEach(track => track.stop());
	
	document.getElementById("status").textContent = "Stopped, transcribing...";
};

