// Recording the Audio
let stream;
let recorder;
let audioChunks = [];

async function startRecording () {
	stream = await navigator.mediaDevices.getUserMedia({
		audio:true
	});
	
	recorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
	audioChunks = [];
	
	recorder.ondataavailable = function(event) {
		audioChunks.push(event.data);
	};
	
	recorder.onstop = async function() {
		document.getElementById("status").textContent = "Processing...";
		
		// Backend for Recording the Audio
		const audioBlob = new Blob(audioChunks, { type: "audio/webm" });
		
		const formData = new FormData();
		formData.append("audio", audioBlob, "userRecording.webm");
	
		const response = await fetch("/api/stt", {
			method: "POST",
			body: formData
		});
		
		const data = await response.json();
		
		document.getElementById("transcription").textContent = data.text;
		document.getElementById("status").textContent = "Not recording";
	};
	
	recorder.start();
	document.getElementById("status").textContent = "Recording...";
}

function stopRecording () {
	recorder.stop();
	stream.getTracks().forEach(track => track.stop());
	
	document.getElementById("status").textContent = "Stopped, transcribing...";
};

