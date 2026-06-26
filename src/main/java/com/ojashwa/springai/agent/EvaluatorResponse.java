package com.ojashwa.springai.agent;

public record EvaluatorResponse(
        boolean isAccurate,
        String reason
) {}