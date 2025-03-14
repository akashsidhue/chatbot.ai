package com.chatbot.ai.chatbot.ai.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
@Log4j
@Service
public class QdrantService {
    private static final String QDRANT_URL = "http://localhost:6333";
    @Autowired
    private AIService aiService;

    // Search for an issue in Qdrant
    public String searchResolution(float[] queryEmbedding,String model) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("""
            {
                "vector": %s,
                "limit": 18,
                 "with_payload": true
            }
            """, java.util.Arrays.toString(queryEmbedding));
            HttpRequest request;
            if(model.equals("mistral")){
                 request = HttpRequest.newBuilder()
                        .uri(URI.create(QDRANT_URL + "/collections/issues/points/search"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
            }
            else{
                 request = HttpRequest.newBuilder()
                        .uri(URI.create(QDRANT_URL + "/collections/issues_llama/points/search"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
            }


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("Qdrant response: %s%n", response.body());
            String solution = extractResolution(response.body());
            System.out.printf("Resolution: %s%n", solution);
            return solution;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error searching issue!";
        }
    }

    // Extract resolution from Qdrant response
    private String extractResolution(String response) {
        JSONObject jsonResponse = new JSONObject(response);

        // Check if the result array exists
        if (jsonResponse.has("result")) {
            JSONArray results = jsonResponse.getJSONArray("result");

            // Ensure at least one result exists
            if (!results.isEmpty()) {
                JSONObject bestMatch = results.getJSONObject(0); // Pick the top match

                // Extract the score
                double score = bestMatch.getDouble("score");

                // Extract the solution from the payload
                if (bestMatch.has("payload")) {
                    JSONObject payload = bestMatch.getJSONObject("payload");
                    if (payload.has("solution")) {
                        String solution = payload.getString("solution");
                        return "Solution: " + solution + " (Score: " + score + ")";
                    }
                }
            }
        }

        return null;
    }
    public Boolean storeIssue(String issue, String solution, float[] embedding,String model) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tableName;
        if(model.equals("mistral")){
             tableName="issues";
        }
        else{
             tableName="issues_llama";
        }
        // Create JSON request
        Map<String, Object> payload = Map.of(
                "collection_name", tableName,
                "points", List.of(
                        Map.of(
                                "id", UUID.randomUUID().toString(),
                                "vector", embedding,
                                "payload", Map.of("issue", issue, "solution", solution)
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(QDRANT_URL + "/collections/"+tableName+"/points", HttpMethod.PUT, entity, String.class);

        // Validate the response
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        } else {
            return false;
        }


    }
    public Boolean storeIssueSentenceTransformer(String issue, String solution, List<Float> embedding) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tableName="issues_sentece_transformer";
        // Create JSON request
        Map<String, Object> payload = Map.of(
                "collection_name", tableName,
                "points", List.of(
                        Map.of(
                                "id", UUID.randomUUID().toString(),
                                "vector", embedding,
                                "payload", Map.of("issue", issue, "solution", solution)
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(QDRANT_URL + "/collections/"+tableName+"/points", HttpMethod.PUT, entity, String.class);

        // Validate the response
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        } else {
            return false;
        }


    }
    public String searchResolutionSentenceTransformer(List<Float> queryEmbedding) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();
            String vectorJson = objectMapper.writeValueAsString(queryEmbedding);

            String jsonBody = String.format("""
        {
            "vector": %s,
            "limit": 18,
            "with_payload": true
        }
        """, vectorJson);
            HttpRequest request= HttpRequest.newBuilder()
                        .uri(URI.create(QDRANT_URL + "/collections/issues_sentece_transformer/points/search"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("Qdrant response: %s%n", response.body());
            String solution = extractResolution(response.body());
            System.out.printf("Resolution: %s%n", solution);
            return solution;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error searching issue!";
        }
    }



}

