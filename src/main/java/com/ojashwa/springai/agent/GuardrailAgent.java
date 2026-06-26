package com.ojashwa.springai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

@Service
public class GuardrailAgent {

    private final ChatClient chatClient;

    public GuardrailAgent(ChatClient.Builder builder) {
        // We use OllamaOptions specifically to force hardware-level JSON generation
        this.chatClient = builder
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.0)
                        .format("json")
                        .build())
                .build();
    }

    public GuardrailResponse evaluate(String userQuery, String documentText) {
        var converter = new BeanOutputConverter<>(GuardrailResponse.class);

        return chatClient.prompt()
                .system(s -> s.text("""
                        You are a strict relevance grader.
                        Read the Document and determine if it contains facts relevant to the User Query.
                        You must output perfectly valid JSON. Do not cut off the output.
                        {format}
                        """)
                        .param("format", converter.getFormat()))
                .user("User Query: " + userQuery + "\n\nDocument: " + documentText)
                .call()
                .entity(converter);
    }
}