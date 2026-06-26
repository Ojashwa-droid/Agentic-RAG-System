package com.ojashwa.springai.controller;

import com.ojashwa.springai.advisors.TokenUsageAuditAdvisor;
import com.ojashwa.springai.agent.EvaluatorAgent;
import com.ojashwa.springai.agent.EvaluatorResponse;
import com.ojashwa.springai.agent.GuardrailDocumentRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final Logger log = LoggerFactory.getLogger(RagController.class);

    private final ChatClient chatClient;
    private final GuardrailDocumentRetriever guardrailRetriever;
    private final EvaluatorAgent evaluatorAgent;
    private final VectorStore vectorStore;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;


    @Value("classpath:promptTemplates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    @Value("classpath:promptTemplates/systemPromptHrPolicyTemplate.st")
    Resource hrSystemTemplate;

    public RagController(@Qualifier("chatClient") ChatClient chatClient,
                         VectorStore vectorStore,
                         EvaluatorAgent evaluatorAgent,
                         GuardrailDocumentRetriever guardrailRetriever,
                         RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.guardrailRetriever = guardrailRetriever;
        this.evaluatorAgent = evaluatorAgent;
        this.retrievalAugmentationAdvisor = retrievalAugmentationAdvisor;
    }

    @GetMapping("/document/chat/v2")
    public ResponseEntity<String> documentChatV2(@RequestParam("message") String message) {
        SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor();
        TokenUsageAuditAdvisor tokenUsageAuditAdvisor = new TokenUsageAuditAdvisor();

        // --- STEP 1: PRE-RETRIEVAL GUARDRAIL ---
        log.info("Starting Agentic Pipeline for query: {}", message);
        List<Document> safeDocuments = guardrailRetriever.retrieve(new Query(message));

        if (safeDocuments.isEmpty()) {
            return new ResponseEntity<>("I do not have that information in my knowledge base.", HttpStatus.OK);
        }

        // Convert documents to a single string context
        String contextStr = safeDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // --- STEP 2 & 3: GENERATION & EVALUATION LOOP ---
        int maxAttempts = 3;
        int attempt = 1;

        while (attempt <= maxAttempts) {
            log.info("Generation Attempt {}/{}", attempt, maxAttempts);

            // Generator creates a draft
            String draftAnswer = chatClient.prompt()
                    .system(s -> s.text("You are an HR Assistant. Answer using ONLY this context:\n" + contextStr))
                    .user(message)
                    .advisors(List.of(simpleLoggerAdvisor, tokenUsageAuditAdvisor))
                    .call()
                    .content();

            // Evaluator audits the draft
            log.info("Sending Draft Answer to Evaluator Agent...");
            EvaluatorResponse evaluation = evaluatorAgent.evaluate(message, contextStr, draftAnswer);

            if (evaluation.isAccurate()) {
                log.info("Evaluator APPROVED the answer.");
                return new ResponseEntity<>(draftAnswer, HttpStatus.OK);
            } else {
                log.warn("Evaluator REJECTED the answer. Reason: {}", evaluation.reason());
                attempt++;
            }
        }

        // --- STEP 4: FAILSAFE ---
        log.error("Pipeline failed after {} attempts. Halting to prevent hallucination.", maxAttempts);
        return new ResponseEntity<>("I am sorry, but I am unable to generate a highly accurate response" +
                                          " for this query at the moment.", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @GetMapping("/document/chat/v1")
    public ResponseEntity<String> documentChatV1(@RequestParam("message") String message) {
        SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor();
        TokenUsageAuditAdvisor tokenUsageAuditAdvisor = new TokenUsageAuditAdvisor();

        String content = chatClient.prompt()
                .user(message)
                .advisors(List.of(simpleLoggerAdvisor, tokenUsageAuditAdvisor, retrievalAugmentationAdvisor))
                .call().content();
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestParam("message") String message) {
       /* SearchRequest searchRequest = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5).build();
          List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

          String similarContext = similarDocs.stream().map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
       */

        String content = chatClient.prompt()
                /*.system(
                        promptSystemSpec -> promptSystemSpec.text(promptTemplate)
                                .param("documents", similarContext))*/
                .user(message)
                .call().content();
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

}
