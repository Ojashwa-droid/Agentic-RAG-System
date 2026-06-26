package com.ojashwa.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptStuffingController {

    private final ChatClient chatClient;

    public PromptStuffingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    @GetMapping("/prompt-stuffing")
    public String promptStuffing(@RequestParam("message") String message) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(systemPromptTemplate))
                .user(message)
                .call().chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }
}
