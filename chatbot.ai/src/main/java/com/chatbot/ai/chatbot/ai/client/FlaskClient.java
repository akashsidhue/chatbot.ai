package com.chatbot.ai.chatbot.ai.client;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class FlaskClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FLASK_URL = "http://127.0.0.1:5000/embed";

    public List<Float> getEmbedding(String text) {
        try {
            // Prepare request body
            Map<String, String> requestBody = Map.of("text", text);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request to Flask API
            ResponseEntity<Map> response = restTemplate.exchange(FLASK_URL, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return (List<Float>) response.getBody().get("embeddings");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null in case of an error
    }
}
