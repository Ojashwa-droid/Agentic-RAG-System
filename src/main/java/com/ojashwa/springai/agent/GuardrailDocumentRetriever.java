package com.ojashwa.springai.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GuardrailDocumentRetriever implements DocumentRetriever {

    private final DocumentRetriever baseRetriever;
    private final GuardrailAgent guardrailAgent;

    // We inject the default VectorStore retriever and our new Guardrail Agent
    public GuardrailDocumentRetriever(DocumentRetriever baseRetriever, GuardrailAgent guardrailAgent) {
        this.baseRetriever = baseRetriever;
        this.guardrailAgent = guardrailAgent;
    }

    @Override
    public List<Document> retrieve(Query query) {
        // 1. Get the raw documents from Qdrant
        List<Document> rawDocuments = baseRetriever.retrieve(query);
        List<Document> approvedDocuments = new ArrayList<>();

        log.info("Guardrail is evaluating {} documents from Qdrant...", rawDocuments.size());

        // 2. Pass each document through the Guardrail Agent
        for (Document doc : rawDocuments) {
            GuardrailResponse evaluation = guardrailAgent.evaluate(query.text(), doc.getText());
            
            if (evaluation.isRelevant()) {
                log.info("✅ Document APPROVED.");
                approvedDocuments.add(doc);
            } else {
                log.warn("❌ Document REJECTED. Reason: {}", evaluation.reason());
            }
        }

        // 3. Return ONLY the approved documents to the RAG Advisor
        return approvedDocuments;
    }
}