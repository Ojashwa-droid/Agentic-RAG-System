package com.ojashwa.springai.config;

import com.ojashwa.springai.advisors.TokenUsageAuditAdvisor;
import com.ojashwa.springai.agent.GuardrailAgent;
import com.ojashwa.springai.agent.GuardrailDocumentRetriever;
import com.ojashwa.springai.rag.PIIMaskingDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        ChatOptions chatOptions = ChatOptions.builder().model("llama3.2:3b").build();

        SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor();
        TokenUsageAuditAdvisor tokenUsageAuditAdvisor = new TokenUsageAuditAdvisor();

        return chatClientBuilder
                .defaultOptions(chatOptions)
//                .defaultAdvisors(List.of(simpleLoggerAdvisor, tokenUsageAuditAdvisor))
                .build();
    }

    @Bean
    public GuardrailDocumentRetriever guardrailDocumentRetriever(VectorStore vectorStore, GuardrailAgent guardrailAgent) {

        // 1. Create the standard 'dumb' Qdrant retriever
        var vectorRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .build();

        // 2. Wrap it in our custom Guardrail logic and return it as a Bean!
        return new GuardrailDocumentRetriever(vectorRetriever, guardrailAgent);
    }


    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore, GuardrailAgent guardrailAgent,
                                                              ChatClient.Builder chatClientBuilder) {

        // 1. Create the standard Qdrant retriever
        var vectorRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .build();

        // 2. Wrap it in our custom Guardrail logic!
        var smartRetriever = new GuardrailDocumentRetriever(vectorRetriever, guardrailAgent);

        // 3. Give the smart retriever to the Advisor
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .targetLanguage("english").build())
                .documentRetriever(smartRetriever)
                .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
                .build();
    }

    //    @Bean
//    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore,
//                                                              ChatClient.Builder chatClientBuilder) {
//        return RetrievalAugmentationAdvisor.builder()
//                .queryTransformers(TranslationQueryTransformer.builder()
//                        .chatClientBuilder(chatClientBuilder.clone())
//                        .targetLanguage("english").build())
//                .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
//                        .topK(3).similarityThreshold(0.5).build())
//                .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
//                .build();
//    }
}
