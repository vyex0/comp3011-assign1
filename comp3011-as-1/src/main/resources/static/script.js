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
	
	recorder.onstop = function() {
		const audioBlob = new Blob(audioChunks);
		
		console.log("Finished Recording");
		console.log(audioBlob);
	};
	
	recorder.start();
	
	console.log("Recording...");
}

function stopRecording () {
	recorder.stop();
	
	stream.getTracks().forEach(track => track.stop());
	
	console.log("Stopped Recording");
};

