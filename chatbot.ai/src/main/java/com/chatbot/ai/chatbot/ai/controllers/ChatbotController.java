package com.chatbot.ai.chatbot.ai.controllers;

import com.chatbot.ai.chatbot.ai.db.model.Issue;
import com.chatbot.ai.chatbot.ai.services.AIService;
import com.chatbot.ai.chatbot.ai.services.QdrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    @Autowired
    private QdrantService qdrantService;
    @Autowired
    private AIService aiService;

    public ChatbotController(QdrantService qdrantService, AIService aiService) {
        this.qdrantService = qdrantService;
        this.aiService = aiService;
    }

    @PostMapping("/resolve")
    public String resolveIssue(@RequestBody Issue issue) {
        float[] queryEmbedding = aiService.getEmbedding(issue.getIssue()); // Convert text to vector
        String resolution = qdrantService.searchResolution(queryEmbedding);

        if (resolution == null || resolution.contains("error")) {
            return "No exact match found. AI-generated resolution: " + aiService.generateResolution(issue.getIssue());
        }
        return "Resolution: " + resolution;
    }

    @PostMapping("/store")
    public ResponseEntity<String> storeIssue(@RequestBody Issue issue) {
        float[] embedding = aiService.getEmbedding(issue.getIssue());  // Convert text to vector
        qdrantService.storeIssue(issue.getIssue(), issue.getSolution(), embedding);
        return ResponseEntity.ok("Issue stored successfully!");
    }
}


