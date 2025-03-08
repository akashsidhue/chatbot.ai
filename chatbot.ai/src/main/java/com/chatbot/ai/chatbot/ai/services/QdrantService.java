package com.chatbot.ai.chatbot.ai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
@Service
public class QdrantService {
    private static final String QDRANT_URL = "http://localhost:6333";
    @Autowired
    private AIService aiService;

    // Search for an issue in Qdrant
    public String searchResolution(float[] queryEmbedding) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("""
            {
                "vector": %s,
                "top": 1
            }
            """, java.util.Arrays.toString(queryEmbedding));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(QDRANT_URL + "/collections/issues/points/search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return extractResolution(response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error searching issue!";
        }
    }

    // Extract resolution from Qdrant response
    private String extractResolution(String response) {
        if (response.contains("\"payload\":")) {
            int startIndex = response.indexOf("\"resolution\":") + 13;
            int endIndex = response.indexOf("\"", startIndex);
            return response.substring(startIndex, endIndex);
        }
        return null;
    }
    public void storeIssue(String issue, String solution, float[] embedding) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create JSON request
        Map<String, Object> payload = Map.of(
                "collection_name", "issues",
                "points", List.of(
                        Map.of(
                                "id", UUID.randomUUID().toString(),
                                "vector", embedding,
                                "payload", Map.of("issue", issue, "solution", solution)
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate.exchange(QDRANT_URL + "/collections/issues/points", HttpMethod.PUT, entity, String.class);
    }


}

