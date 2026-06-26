package com.ojashwa.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("You are a helpful assistant that responds to user queries").build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return chatClient
                .prompt()
                .user(message)
                .call().content();
    }
}
