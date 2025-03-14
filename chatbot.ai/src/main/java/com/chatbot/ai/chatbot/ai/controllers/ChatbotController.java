package com.chatbot.ai.chatbot.ai.controllers;

import com.chatbot.ai.chatbot.ai.db.model.Issue;
import com.chatbot.ai.chatbot.ai.services.AIService;
import com.chatbot.ai.chatbot.ai.services.EmbeddingComparison;
import com.chatbot.ai.chatbot.ai.services.IssueEmbeddingService;
import com.chatbot.ai.chatbot.ai.services.QdrantService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    @Autowired
    private QdrantService qdrantService;
    @Autowired
    private AIService aiService;

    @Autowired
    private EmbeddingComparison embeddingComparison;

    @Autowired
    private IssueEmbeddingService issueEmbeddingService;

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

    @GetMapping("/compareEmbeddings")
    public double compareEmbeddings(@RequestParam("text1") String text1, @RequestParam("text2") String text2, @RequestParam("model") String model) {
        float[] embedding1 = aiService.getEmbedding(embeddingComparison.normalize(text1),model);
        float[] embedding2 = aiService.getEmbedding(embeddingComparison.normalize(text2),model);
        return embeddingComparison.cosineSimilarity(embedding1, embedding2);
    }

    @GetMapping("/getEmbeddingSentenceTransformers")
    public List<?> getEmbeddingSentenceTransformers(@RequestParam("text") String text) {
        return issueEmbeddingService.processIssue(text);
    }

    @PostMapping("/storeSentenceTransformer")
    public ResponseEntity<?> storeEmbeddingSentenceTransformer(@RequestBody Issue issue) {
        List<Float> embedding= issueEmbeddingService.processIssue(issue.getIssue());
        Boolean response_status=qdrantService.storeIssueSentenceTransformer(issue.getIssue(),issue.getSolution(),embedding);
        if (response_status){
            return ResponseEntity.ok("Issue stored successfully! "+issue.getIssue());
        }
        return ResponseEntity.ok("issue was not stored");
    }

    @GetMapping("/compareEmbeddingsSentenceTransformers")
    public double compareEmbeddingsSentenceTransformers(@RequestParam("text1") String text1, @RequestParam("text2") String text2) {
        List<Float> embedding1 = issueEmbeddingService.processIssue(text1);
        List<Float> embedding2 = issueEmbeddingService.processIssue(text2);
        return embeddingComparison.cosineSimilarity(embedding1, embedding2);
    }

    @PostMapping("/resolveUsingSentenceTransformer")
    public String resolveUsingSentenceTransformer(@RequestBody Issue issue) {
        List<Float> embedding = issueEmbeddingService.processIssue(issue.getIssue());
        String resolution = qdrantService.searchResolutionSentenceTransformer(embedding); // Search for issue in Qdrant
        String model="llama3";
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

}


