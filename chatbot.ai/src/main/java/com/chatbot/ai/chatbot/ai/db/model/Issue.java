package com.chatbot.ai.chatbot.ai.db.model;

import lombok.*;

@Data
public class Issue {
    public String issue;
    public String solution;

    public Issue() {
    }

    public Issue(String issue, String solution) {
        this.issue = issue;
        this.solution = solution;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    }
