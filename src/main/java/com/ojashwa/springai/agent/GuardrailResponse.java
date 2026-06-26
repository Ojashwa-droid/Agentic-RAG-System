package com.ojashwa.springai.agent;

public record GuardrailResponse(
        boolean isRelevant,
        String reason
) {}