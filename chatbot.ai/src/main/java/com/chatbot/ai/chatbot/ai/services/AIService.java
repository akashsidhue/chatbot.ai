package com.chatbot.ai.chatbot.ai.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Service
public class AIService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String OLLAMA_EMBEDDING_URL = "http://localhost:11434/api/embeddings"; // Adjust based on your setup


    // Generate a resolution using local LLM
    public String generateResolution(String issue) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
        {
            "model": "mistral",
            "prompt": "Provide a resolution for the following issue: %s",
            "stream": false
        }
        """.formatted(issue);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OLLAMA_API_URL, HttpMethod.POST, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("response")) {
            return response.getBody().get("response").toString();
        }
        return "Error generating resolution.";
    }
    public float[] getEmbedding(String text) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Send text to Ollama to generate an embedding
        Map<String, String> request = Map.of("model", "llama3", "prompt", text);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_EMBEDDING_URL, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("embedding")) {
            Object embeddingObject = response.getBody().get("embedding");

            if (embeddingObject instanceof List<?>) {
                List<?> embeddingList = (List<?>) embeddingObject;

                float[] floatArray = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    Object value = embeddingList.get(i);
                    if (value instanceof Number) {
                        floatArray[i] = ((Number) value).floatValue();
                    } else {
                        return new float[0]; // Return empty if unexpected type is found
                    }
                }
                return floatArray;
            }
        }
        return new float[768]; // Default size (depends on model)
    }
}

