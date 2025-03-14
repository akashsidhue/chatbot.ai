package com.chatbot.ai.chatbot.ai.controllers;

import com.chatbot.ai.chatbot.ai.db.model.Issue;
import com.chatbot.ai.chatbot.ai.services.AIService;
import com.chatbot.ai.chatbot.ai.services.QdrantService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Log4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    @Autowired
    private QdrantService qdrantService;
    @Autowired
    private AIService aiService;

    @PostMapping("/resolve")
    public String resolveIssue(@RequestBody Issue issue, @RequestParam("model") String model) {
        float[] queryEmbedding = aiService.getEmbedding(issue.getIssue(),model); // Convert text to vector
        String resolution = qdrantService.searchResolution(queryEmbedding,model); // Search for issue in Qdrant
        if (resolution == null || resolution.contains("error") ) {

            return "No exact match found. AI-generated resolution: " + aiService.generateResolution(issue.getIssue(),model);
        }

        String[] parts = resolution.split("\\(Score: ");
        String score = parts.length > 1 ? parts[1].replace(")", "") : "N/A";
        if(Double.parseDouble(score)<0.4){
            return "No exact match found. AI-generated resolution: " + aiService.generateResolution(issue.getIssue(),model);
        }
        if(Double.parseDouble(score)<0.5 && Double.parseDouble(score)>=0.4){
            return "partial Match Found : " +resolution + "|| Also attaching AI generated response"+ aiService.generateResolution(issue.getIssue() ,model);
        }
        return resolution;

    }

    @PostMapping("/store")
    public ResponseEntity<?> storeIssue(@RequestBody Issue issue, @RequestParam("model") String model) {
        float[] embedding = aiService.getEmbedding(issue.getIssue(),model);  // Convert text to vector
        Boolean response_status=qdrantService.storeIssue(issue.getIssue(), issue.getSolution(), embedding,model);
        if(response_status){
            return ResponseEntity.ok("Issue stored successfully! "+issue.getIssue());
        }
        return ResponseEntity.ok("issue was not stored");

    }

    @GetMapping("/getEmbedding")
    public float[] getEmbedding(@RequestParam("text") String text, @RequestParam("model") String model) {
        return aiService.getEmbedding(text,model);
    }
}


