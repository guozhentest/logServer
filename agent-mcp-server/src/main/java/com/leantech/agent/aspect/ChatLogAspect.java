package com.leantech.agent.aspect;

import com.leantech.agent.annotation.ChatLog;
import com.leantech.agent.doc.ChatMessageDoc;
import com.leantech.agent.service.AgentTelemetryService;
import com.leantech.agent.service.ChatHistoryService;
import com.leantech.agent.service.ChatMemoryService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class ChatLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ChatLogAspect.class);

    private final ChatMemoryService chatMemoryService;
    private final ChatHistoryService chatHistoryService;
    private final AgentTelemetryService telemetryService;
    private final String modelName;

    public ChatLogAspect(ChatMemoryService chatMemoryService,
                         ChatHistoryService chatHistoryService,
                         AgentTelemetryService telemetryService,
                         @Value("${spring.ai.ollama.chat.options.model}") String modelName) {
        this.chatMemoryService = chatMemoryService;
        this.chatHistoryService = chatHistoryService;
        this.telemetryService = telemetryService;
        this.modelName = modelName;
    }

    @Around("@annotation(chatLog)")
    public Object around(ProceedingJoinPoint joinPoint, ChatLog chatLog) throws Throwable {
        long start = System.currentTimeMillis();
        String conversationId = UUID.randomUUID().toString().substring(0, 8);
        String message = null, userId = null, answer = null;
        int tokensInput = 0, tokensOutput = 0;

        // 提取方法参数
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            message = (String) args[0];
            userId = (String) args[1];
        }

        try {
            // 保存用户消息到 MongoDB
            if (chatLog.saveMemory() && message != null && userId != null) {
                chatMemoryService.saveMessage(userId, conversationId, "USER", message);
            }

            // 执行原方法
            Object result = joinPoint.proceed();
            answer = result instanceof String ? (String) result : null;
            long cost = System.currentTimeMillis() - start;

            // 保存助手回复到 MongoDB
            if (chatLog.saveMemory() && answer != null && userId != null) {
                chatMemoryService.saveMessage(userId, conversationId, "ASSISTANT", answer);
            }

            // 保存到 MySQL 审计表
            if (chatLog.saveHistory() && message != null && answer != null) {
                chatHistoryService.saveChatHistory(conversationId, message, answer, modelName,
                        (int) cost, tokensInput, tokensOutput, "success", null);
            }

            // 遥测
            if (message != null && answer != null) {
                telemetryService.finishInteraction(conversationId, answer, (int) cost);
            }

            log.info("聊天记录已异步保存, conversationId={}, 耗时={}ms", conversationId, cost);
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            if (message != null) {
                telemetryService.recordError(conversationId, e.getMessage());
                chatHistoryService.saveChatHistory(conversationId, message, null, modelName,
                        (int) cost, tokensInput, tokensOutput, "error", e.getMessage());
            }
            log.error("聊天记录保存失败, conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw e;
        }
    }
}