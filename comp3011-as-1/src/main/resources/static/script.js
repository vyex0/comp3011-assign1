let stream;
let recorder;
let audioChunks = [];

async function startRecording () {
	document.getElementById("status").textContent = "Requesting microphone access...";
	try {
		// Prompts the user's browser for mic permission then store
		// the mic stream.
		stream = await navigator.mediaDevices.getUserMedia({
			audio:true
		});
	} catch (error) {
		// Handles permission denial: no mic found or browser is
		// blocking mic access. Returns if there's an error.
		document.getElementById("status").textContent =
			"Couldn't get access to the microhphone. Please check permissions and try again!";
		return;
	};
	
	// Disables the startBtn so that the user isn't able to click on the
	// start button again after recording has been started.
	document.getElementById("startBtn").disabled = true;
	document.getElementById("stopBtn").disabled = false;
	
	// MediaRecorder is the browser API used to record the MediaStream,
	// it is used to capture the user's audio input. Ensures the audio
	// has the audio/webm mimeType format and stores the audio chunks into
	// the empty audioChunks array.
	try {
		recorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
	} catch(error) {
		document.getElementById("status").textContent =
			"Recording isn't supported in this browser. Please try Chrome or Firefox.";
		stream.getTracks().forEach(track => track.stop());
		document.getElementById("startBtn").disabled = false;
		document.getElementById("stopBtn").disabled = true;
		return;
	}
	audioChunks = [];
	
	// Whenever recorder has a piece of audio available, run this
	// function so that the audios are stored in audioChunks.
	recorder.ondataavailable = function(event) {
		audioChunks.push(event.data);
	};
	
	// When the recorder is stopped, run this function so the status can
	// be updated to no longer recording, the audio chunks are combined,
	// and audio is attached to the formData.
	recorder.onstop = async function() {
		document.getElementById("status").textContent = "Processing transcription...";
		
		// Combines all the audio chunks into one audio file with mimeType audio/webm
		const audioBlob = new Blob(audioChunks, { type: "audio/webm" });
		
		// Creates the form style data for a HTTP request, and attaches
		// the audio file to the form.
		const formData = new FormData();
		formData.append("audio", audioBlob, "userRecording.webm");
	
		// Sends it to my SttService backend and waits for the JSON response.
		try {
			const response = await fetch("/stt", {
				method: "POST",
				body: formData
			});
			
			const data = await response.json();
			
			if (!response.ok) {
				// Returns if backend didn't return a successful HTTP status.
				document.getElementById("transcription").textContent = "";
				document.getElementById("status").textContent = 
					"Transcription failed: " + (data.message || "please try again.");
				document.getElementById("startBtn").disabled = false;
				return;
			};
			
			// Displays the transcribed text and resets the status, ensuring
			// application is ready for a new recording without page reload.
			document.getElementById("transcription").textContent = data.text;
			document.getElementById("status").textContent = "Not recording";
			document.getElementById("startBtn").disabled = false;
		} catch(error) {
			// fetch itself threw: server unreachable, connection dropped.
			document.getElementById("transcription").textContent = "";
			document.getElementById("status").textContent = 
				"Network error: Couldn't reach the server. Please try again.";
			document.getElementById("startBtn").disabled = false;
		};
	};
	
	// Starts recording the user's audio and changes the status to recording.
	recorder.start();
	document.getElementById("recIndicator").classList.add("active");
	document.getElementById("status").textContent = "Recording...";
}

function stopRecording () {
	recorder.stop();
	
	// Stops recording on all of the tracks inside the user's browser,
	// but since we're only using the audio track, audio track is the only
	// one being stopped from streaming. Basically release the mic.
	// Releases the user's mic so the browser's recording
	// indicator turns off and mic isn't reserved anymore after use. 
	stream.getTracks().forEach(track => track.stop());
	
	document.getElementById("recIndicator").classList.remove("active");
	document.getElementById("status").textContent = "Stopped, transcribing...";
	
	// startBtn stays disabled until current transcription finishes.
	document.getElementById("stopBtn").disabled = true;
};

