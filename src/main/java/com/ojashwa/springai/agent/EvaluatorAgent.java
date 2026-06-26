package com.ojashwa.springai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

@Service
public class EvaluatorAgent {

    private final ChatClient chatClient;

    public EvaluatorAgent(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.0)
                        .format("json")
                        .build())
                .build();
    }

    public EvaluatorResponse evaluate(String userQuery, String context, String generatedAnswer) {
        var converter = new BeanOutputConverter<>(EvaluatorResponse.class);

        return chatClient.prompt()
                .system(s -> s.text("""
                        You are a strict Auditor. Your job is to check if the Generated Answer aligns with the Context.
                        
                        RULES:
                        1. If the Answer contains rules, numbers, or policies that completely contradict the Context, return false.
                        2. If the Answer invents entirely new company policies not mentioned in the Context, return false.
                        3. If the Answer is a safe refusal (e.g., "I don't know"), return true.
                        4. If the Answer accurately reflects the Context, return true.
                        
                        Do not be overly strict about exact phrasing. Focus on factual accuracy.
                        You must output perfectly valid JSON.
                        {format}
                        """)
                        .param("format", converter.getFormat()))
                // Use XML tags to help the 3B model separate the text!
                .user(String.format("""
                        <query>%s</query>
                        
                        <context>
                        %s
                        </context>
                        
                        <answer>
                        %s
                        </answer>
                        """, userQuery, context, generatedAnswer))
                .call()
                .entity(converter);
    }
}