package com.leantech.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgentTelemetryService {

    private static final Logger log = LoggerFactory.getLogger(AgentTelemetryService.class);

    public String startInteraction(String message) {
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        log.info("interaction.start conversationId={}, questionLength={}", conversationId, message != null ? message.length() : 0);
        return conversationId;
    }

    @Async
    public void recordLlmRequest(String conversationId, String modelName, int tokensInput, int tokensOutput, int durationMs) {
        log.info("interaction.llm conversationId={}, model={}, promptTokens={}, completionTokens={}, durationMs={}",
                conversationId, modelName, tokensInput, tokensOutput, durationMs);
    }

    @Async
    public void finishInteraction(String conversationId, String answer, int durationMs) {
        log.info("interaction.finish conversationId={}, answerLength={}, durationMs={}",
                conversationId, answer != null ? answer.length() : 0, durationMs);
    }

    @Async
    public void recordError(String conversationId, String errorMessage) {
        log.warn("interaction.error conversationId={}, error={}", conversationId, errorMessage);
    }
}
