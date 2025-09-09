package com.example.MyTwitchBot.Model;


import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class TwitchConfig {

    @Value("${twitch.client.id}")
    private String clientId;

    @Value("${twitch.client.secret}")
    private String clientSecret;

    @Value("${twitch.access.token}")
    private String accessToken;

    @Value("${twitch.channel.name}")
    private String channelName;

    private TwitchClient twitchClient;
    private Poll currentPoll = null;


    @PostConstruct
    public void connectToTwitch() {
        System.out.println("Attempting to connect to Twitch...");
        System.out.println("Client ID: " + clientId);
        System.out.println("Channel: " + channelName);

        OAuth2Credential credential = new OAuth2Credential("twitch", accessToken);

        twitchClient = TwitchClientBuilder.builder()
                .withClientId(clientId)
                .withEnableChat(true)
                .withChatAccount(credential)
                .withEnableHelix(true)
                .build();

        twitchClient.getChat().joinChannel(channelName);
        System.out.println("Bot connected to channel: " + channelName);

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, this::handleMessage);

        // Test message
        twitchClient.getChat().sendMessage(channelName, "Hello! Bot is now online! 🤖");
    }

    private void sendMessage(String message) {
        twitchClient.getChat().sendMessage(channelName, message);
    }

    private boolean isValidVoteOption(String message) {
        if (currentPoll == null) return false;

        String cleanMessage = message.toLowerCase().trim();
        for (String option : currentPoll.getOptions()) {
            if (option.toLowerCase().equals(cleanMessage)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPoll() {
        return currentPoll != null && currentPoll.isActive();
    }

    private void endPoll() {
        showResults();

        // Find winner using YOUR Poll class
        Map<String, Integer> results = currentPoll.getResults();
        String winner = results.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No votes");

        currentPoll.setActive(false);
        currentPoll = null;
        System.out.println("🏁 Poll ended");
    }

    private void showResults() {
        Map<String, Integer> results = currentPoll.getResults();
        int total = currentPoll.getTotalVotes();

        StringBuilder msg = new StringBuilder("📊 RESULTS: ");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            double percentage = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            msg.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(" (")
                    .append(String.format("%.1f%%", percentage))
                    .append(") | ");
        }
        msg.append("Total: ").append(total);

        sendMessage(msg.toString());
    }

    private void handleVote(String username, String vote) {
        // Direct vote - no parsing needed, just the raw option
        String cleanVote = vote.toLowerCase().trim();

        // Check if this is a valid option for current poll (simple loop)
        String actualOption = null;
        for (String option : currentPoll.getOptions()) {
            if (option.toLowerCase().equals(cleanVote)) {
                actualOption = option; // Keep original casing
                break;
            }
        }

        if (actualOption != null) {

            String currentVote = currentPoll.getVotes().get(username);

            if (!actualOption.equals(currentVote)) {

                currentPoll.recordVote(username, actualOption);

                if (currentVote != null) {
                    System.out.println("🔄 " + username + " changed vote from " + currentVote + " to " + actualOption);
                } else {
                    System.out.println("🗳️ " + username + " voted for: " + actualOption);
                }
            }

        }
    }

    private void createPoll(String pollData) {
        if (hasPoll()) {
            sendMessage("❌ Poll already active! Use !endpoll first.");
            return;
        }

        try {
            // Parse: "Question?" option1,option2,option3
            String[] parts = pollData.split("\"");
            if (parts.length < 2) {
                sendMessage("❌ Usage: !startpoll \"Question?\" option1,option2,option3");
                return;
            }

            String question = parts[1]; // Question is between quotes
            String optionsStr = parts.length > 2 ? parts[2].trim() : "";

            if (optionsStr.isEmpty()) {
                sendMessage("❌ Usage: !startpoll \"Question?\" option1,option2,option3");
                return;
            }

            // Split options by comma
            List<String> options = Arrays.asList(optionsStr.split(","));
            options = options.stream().map(String::trim).collect(Collectors.toList());

            if (options.size() < 2) {
                sendMessage("❌ Need at least 2 options separated by commas");
                return;
            }

            // Use YOUR Poll class
            currentPoll = new Poll(null, question, options, true, new HashMap<>());
            // No scanning needed - Twitch4J delivers messages automatically!

            // Announce poll
            StringBuilder msg = new StringBuilder("📊 POLL: " + question + " | Vote by typing: ");
            msg.append(String.join(", ", options));

            sendMessage(msg.toString());
            System.out.println("✅ Poll created: " + question + " | Options: " + options);

        } catch (Exception e) {
            sendMessage("❌ Error creating poll. Use: !startpoll \"Question?\" option1,option2,option3");
        }
    }

    private void handleMessage(ChannelMessageEvent event) {
        String message = event.getMessage().trim();
        String username = event.getUser().getName();

        System.out.println(username + ": " + message);

        // Handle commands
        if (message.startsWith("!startpoll ")) {
            createPoll(message.substring(11)); // Remove "!startpoll "
        }
        else if (hasPoll() && isValidVoteOption(message)) {
            handleVote(username, message); // Direct vote like "yes", "no", "banana"
        }
        else if (message.equals("!endpoll") && hasPoll()) {
            endPoll();
        }
    }

    public Poll getCurrentPoll() {
        return currentPoll;
    }




}
