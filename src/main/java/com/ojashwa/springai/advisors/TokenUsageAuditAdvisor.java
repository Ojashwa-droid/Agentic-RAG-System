package com.ojashwa.springai.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenUsageAuditAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    @SuppressWarnings("NullableProblems")
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse.getMetadata() != null) {
            Usage usage = chatResponse.getMetadata().getUsage();

            if (usage != null) {
                logger.info("Token usage audit: {}", usage);
            }
        }
        return chatClientResponse;
    }

    /**
     * Return the name of the advisor.
     *
     * @return the advisor name.
     */
    @Override
    public String getName() {
        return TokenUsageAuditAdvisor.class.getName();
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
