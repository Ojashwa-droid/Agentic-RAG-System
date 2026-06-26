package com.ojashwa.springai.cli.terminal;


import com.ojashwa.springai.controller.ChatController;
import com.ojashwa.springai.controller.RagController;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class RagCliCommands {

    private final ChatController chatController;
    private final RagController ragController;

    public RagCliCommands(ChatController chatController, RagController ragController) {
        this.chatController = chatController;
        this.ragController = ragController;
    }


    @Command(name = "status", description = "Check the status of the Agentic RAG system")
    public String systemStatus() {
        return "Agentic RAG System Online. Ready for queries.";
    }

    @Command(name = "chat", description = "Chat with the LLM model. Model in usage: llama3.2:3b")
    public String chatWithLLMModel(
            @Option(shortName = 'p',longName = "prompt", defaultValue = "Who are you and how can you help me?") String prompt) {

        System.out.println("Please wait...");
        String response = chatController.chat(prompt);

        return "\n--- FINAL RESPONSE ---\n" +
                "Based on the verified context, you asked about: '" + prompt + "'.\n" +
                "The multi-agent pipeline has successfully processed this query: \n" + response;
    }


    @Command(name = "rag-v2", description = "Query the agentic RAG system regarding HR policies of OjasCorporation.")
    public String askAgenticRagV2(
            @Option(shortName = 'p', longName = "prompt") String prompt) {

        System.out.println("Agentic Pipeline activated. Please wait...");
        ResponseEntity<String> responseEntity = ragController.documentChatV2(prompt);
        String response = responseEntity.getBody();

        // Check if the Controller returned a 200 OK (Success)
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return "\n--- FINAL RESPONSE ---\n" +
                    "User Query: '" + prompt + "'\n" +
                    "Status: The multi-agent pipeline successfully verified and generated this response.\n\n" +
                    response;
        }
        // If it returned 500 INTERNAL_SERVER_ERROR (Failsafe triggered)
        else {
            return "\n---  PIPELINE HALTED ---\n" +
                    "User Query: '" + prompt + "'\n" +
                    "Status: Evaluator Agents intercepted the generation to prevent hallucination.\n\n" +
                    "System Message: " + response;
        }
    }

    @Command(name = "rag-v1", description = "Query the agentic RAG system regarding HR policies of OjasCorporation.")
    public String askAgenticRagV1(
            @Option(shortName = 'p',longName = "prompt") String prompt) {

        System.out.println("Please wait...");
        ResponseEntity<String> responseEntity = ragController.documentChatV1(prompt);
        String response = responseEntity.getBody();

        return "\n--- FINAL RESPONSE ---\n" +
                "Based on the verified context, you asked about: '" + prompt + "'.\n" +
                "The multi-agent pipeline has successfully processed this query: \n" + response;
    }
}