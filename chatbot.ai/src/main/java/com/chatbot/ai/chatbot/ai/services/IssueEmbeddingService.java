package com.chatbot.ai.chatbot.ai.services;

import com.chatbot.ai.chatbot.ai.client.FlaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IssueEmbeddingService {

    @Autowired
    private FlaskClient flaskClient;

    public List<Float> processIssue(String issueText) {
        List<Float> embedding = flaskClient.getEmbedding(issueText);
        if (embedding != null) {
            System.out.printf(embedding.size()+" : size");
            return embedding;
        } else {
            System.out.println("Failed to generate embedding");
            return null;
        }

    }
}
