// Recording the Audio
let stream;
let recorder;
let audioChunks = [];

async function startRecording () {
	stream = await navigator.mediaDevices.getUserMedia({
		audio:true
	});
	
	recorder = new MediaRecorder(stream);
	audioChunks = [];
	
	recorder.ondataavailable = function(event) {
		audioChunks.push(event.data);
	};
	
	recorder.onstop = async function() {
		const audioBlob = new Blob(audioChunks);
		
		const formData = new FormData();
		formData.append("audio", audioBlob, "userRecording.webm");
	
		const response = await fetch("/api/stt", {
			method: "POST",
			body: formData
		});
		
		console.log(response);
	};
	
	recorder.start();
	
	console.log("Recording...");
}

function stopRecording () {
	recorder.stop();
	
	stream.getTracks().forEach(track => track.stop());
	
	console.log("Stopped Recording");
};

