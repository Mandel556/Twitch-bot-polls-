package com.example.MyTwitchBot.Controller;

import com.example.MyTwitchBot.Model.Poll;
import com.example.MyTwitchBot.Model.TwitchConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class PollController {

    @Autowired
    private TwitchConfig twitchConfig;

    @GetMapping("/poll/current")
    public Poll getCurrentPoll() {
        return twitchConfig.getCurrentPoll();
    }

    @GetMapping("/poll/results")
    public Map<String, Integer> getCurrentResults() {
        Poll currentPoll = twitchConfig.getCurrentPoll();
        return currentPoll != null ? currentPoll.getResults() : new HashMap<>();
    }

    @GetMapping("/poll/status")
    public Map<String, Object> getPollStatus() {
        Poll currentPoll = twitchConfig.getCurrentPoll();
        Map<String, Object> status = new HashMap<>();

        if (currentPoll != null) {
            status.put("active", currentPoll.isActive());
            status.put("question", currentPoll.getQuestion());
            status.put("options", currentPoll.getOptions());
            status.put("totalVotes", currentPoll.getTotalVotes());
            status.put("results", currentPoll.getResults());
        } else {
            status.put("active", false);
        }

        return status;
    }

}
