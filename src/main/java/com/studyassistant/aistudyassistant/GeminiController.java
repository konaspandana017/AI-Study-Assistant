
package com.studyassistant.aistudyassistant;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class GeminiController {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Existing text API
    @GetMapping("/ask")
    public Map<String, String> askGemini(@RequestParam String prompt) {

        RestClient client = RestClient.create();

        Map<String, Object> request = Map.of(
            "model", "gemini-3.5-flash-lite",
            "input", prompt
        );

        Map response = client.post()
            .uri("https://generativelanguage.googleapis.com/v1beta/interactions")
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);

        List<Map<String, Object>> steps =
            (List<Map<String, Object>>) response.get("steps");

        for (Map<String, Object> step : steps) {

            if ("model_output".equals(step.get("type"))) {

                List<Map<String, Object>> content =
                    (List<Map<String, Object>>) step.get("content");

                for (Map<String, Object> item : content) {

                    if ("text".equals(item.get("type"))) {
                        return Map.of("answer", (String) item.get("text"));
                    }
                }
            }
        }

        return Map.of("answer", "No text response received.");
    }


    // New image API
    @PostMapping("/ask-image")
    public Map<String, String> askAboutImage(
            @RequestParam String prompt,
            @RequestPart("image") MultipartFile image) throws IOException {

        String base64Image = Base64.getEncoder()
                .encodeToString(image.getBytes());

        Map<String, Object> textInput = Map.of(
            "type", "text",
            "text", prompt
        );

        Map<String, Object> imageInput = Map.of(
            "type", "image",
            "data", base64Image,
            "mime_type", image.getContentType()
        );

        Map<String, Object> request = Map.of(
            "model", "gemini-3.5-flash-lite",
            "input", List.of(textInput, imageInput)
        );

        RestClient client = RestClient.create();

        Map response = client.post()
            .uri("https://generativelanguage.googleapis.com/v1beta/interactions")
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);

        List<Map<String, Object>> steps =
            (List<Map<String, Object>>) response.get("steps");

        for (Map<String, Object> step : steps) {

            if ("model_output".equals(step.get("type"))) {

                List<Map<String, Object>> content =
                    (List<Map<String, Object>>) step.get("content");

                for (Map<String, Object> item : content) {

                    if ("text".equals(item.get("type"))) {
                        return Map.of("answer", (String) item.get("text"));
                    }
                }
            }
        }

        return Map.of("answer", "No response received.");
    }
}
