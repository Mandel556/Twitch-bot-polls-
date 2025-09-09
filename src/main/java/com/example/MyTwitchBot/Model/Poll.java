package com.example.MyTwitchBot.Model;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Poll {
    private String id;
    private String question;
    private List<String> options;
    private boolean active;
    private Map<String, String> votes = new HashMap<>();

    public Poll(String id ,String question, List<String> options, boolean active, Map<String, String> votes) {
        this.id = java.util.UUID.randomUUID().toString();
        this.question = question;
        this.options = options;
        this.active = active;
        this.votes = votes;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getVotes() {
        return votes;
    }

    public void setVotes(Map<String, String> votes) {
        this.votes = votes;
    }

    public int getTotalVotes() {
        return votes.size();
    }

    public void startScanning() {
        this.active = true;
    }

    public void stopScanning() {
        this.active = false;
    }

    public boolean isVoteValid(String vote) {
        // Check if the vote matches one of our options
        return options.contains(vote);
    }

    public void recordVote(String username, String vote) {
        // Record or update this user's vote
        votes.put(username, vote);
    }

    public Map<String, Integer> getResults() {
        // Count votes for each option
        Map<String, Integer> results = new HashMap<>();

        // Initialize all options with 0 votes
        for (String option : options) {
            results.put(option, 0);
        }

        // Count votes
        for (String vote : votes.values()) {
            results.put(vote, results.getOrDefault(vote, 0) + 1);
        }

        return results;
    }

}
