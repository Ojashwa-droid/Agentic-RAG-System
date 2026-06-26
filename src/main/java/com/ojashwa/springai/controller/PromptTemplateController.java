package com.ojashwa.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptTemplateController {

    private final ChatClient chatClient;

    public PromptTemplateController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

/*    String promptTemplate = """
            A customer named {customerName} sent the following message:
            "{customerMessage}"
            
            Write a polite and helpful email response addressing the issue.
            Maintain a professional tone and provide reassurance.
            
            Respond as if you're writing the email body only. Don't include subject, signature.
            """;*/

    @Value("classpath:/promptTemplates/userPromptTemplate.st")
    Resource userPromptTemplate;

    @GetMapping("/email")
    public String emailResponse(@RequestParam("customerName") String customerName,
                                @RequestParam("customerMessage") String customerMessage) {
        return chatClient
                .prompt()
                .options(OllamaOptions.builder()
                        .model(OllamaModel.LLAMA3_2_3B).temperature(0.8).build())
//                .advisors(new TokenUsageAuditAdvisor()) Commented due to the reason that advisors are generally for default
                // behavior for all RestAPIs; it's better to use default advisors concept to define our advisors at the chat client bean.
                .system("""
                        You are a professional customer service assistant which helps drafting email
                        responses to improve the productivity of the customer support team.
                        """)
                .user(promptUserSpec -> promptUserSpec.text(userPromptTemplate)
                        .param("customerName", customerName)
                        .param("customerMessage", customerMessage))
                .call().content();
    }
}
