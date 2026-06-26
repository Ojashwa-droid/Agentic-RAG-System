package com.ojashwa.springai.rag;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HRPolicyLoader {
    private final VectorStore vectorStore;

    @Value("classpath:Eazybytes_HR_Policies.pdf")
    Resource hrPolicyPdfFile;

    public HRPolicyLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadPDF(){
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(hrPolicyPdfFile);
        List<Document> rawDocuments = tikaDocumentReader.get();
        log.info("Loaded {} documents", rawDocuments.size());

/*        TextSplitter textSplitter = new TokenTextSplitter(250, 50, 50, 10000, true); */
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(250)
                .withMaxNumChunks(400)
                .withMinChunkSizeChars(50)
                .withKeepSeparator(true)
                .build();
        List<Document> chunkedDocuments = textSplitter.split(rawDocuments);

        log.info("Loaded {} documents", chunkedDocuments.size());

        String rawDocString = rawDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        log.info("Loaded {} documents", rawDocString);

        vectorStore.add(chunkedDocuments);
    }

}








