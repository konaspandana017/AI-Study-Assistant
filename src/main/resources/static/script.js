
let selectedImage = null;

document.getElementById("imageInput").addEventListener("change", function() {

    selectedImage = this.files[0];

    let preview = document.getElementById("preview");
    preview.innerHTML = "";

    if (selectedImage) {
        let img = document.createElement("img");
        img.src = URL.createObjectURL(selectedImage);
        preview.appendChild(img);
    }
});

async function askAI() {

    let input = document.getElementById("question");
    let question = input.value.trim();

    if (!question && !selectedImage) {
        return;
    }

    if (selectedImage && !question) {
        question = "Describe this image.";
    }

    let chat = document.getElementById("chat");

    let userMessage = document.createElement("div");
    userMessage.className = "user-message";

    if (selectedImage) {
        let img = document.createElement("img");
        img.src = URL.createObjectURL(selectedImage);
        img.className = "chat-image";
        userMessage.appendChild(img);
    }

    let text = document.createElement("div");
    text.innerText = question;
    userMessage.appendChild(text);

    chat.appendChild(userMessage);

    let imageToSend = selectedImage;

    input.value = "";
    selectedImage = null;
    document.getElementById("imageInput").value = "";
    document.getElementById("preview").innerHTML = "";

    let botMessage = document.createElement("div");
    botMessage.className = "bot-message";
    botMessage.innerText = "Thinking...";
    chat.appendChild(botMessage);

    chat.scrollTop = chat.scrollHeight;


	try {

	    let response;

	    if (imageToSend) {

	        let formData = new FormData();
	        formData.append("prompt", question);
	        formData.append("image", imageToSend);

	        response = await fetch("/ask-image", {
	            method: "POST",
	            body: formData
	        });

	    } else {

	        response = await fetch(
	            "/ask?prompt=" + encodeURIComponent(question)
	        );
	    }

	    let result = await response.text();

	    if (!response.ok) {
	        throw new Error("HTTP " + response.status + ": " + result);
	    }

	    let data = JSON.parse(result);
	    botMessage.innerText = data.answer;

	} catch (error) {

	    console.error("AI Error:", error);
	    botMessage.innerText = error.message;

	}


    chat.scrollTop = chat.scrollHeight;
}
