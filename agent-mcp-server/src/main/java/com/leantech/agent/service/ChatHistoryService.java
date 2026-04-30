package com.leantech.agent.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);
    private static final int QUEUE_CAPACITY = 5000;
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;
    private final LinkedBlockingQueue<ChatHistoryRecord> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    public ChatHistoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveChatHistory(String conversationId, String question, String answer, String model,
                                int durationMs, int tokensInput, int tokensOutput, String status, String errorMsg) {
        ChatHistoryRecord record = new ChatHistoryRecord(
                conversationId, question, answer, model, durationMs, tokensInput, tokensOutput, status, errorMsg
        );

        if (!queue.offer(record)) {
            log.warn("聊天历史队列已满，降级为即时写入: conversationId={}", conversationId);
            insertBatch(List.of(record));
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void flush() {
        List<ChatHistoryRecord> batch = new ArrayList<>(BATCH_SIZE);
        queue.drainTo(batch, BATCH_SIZE);
        if (!batch.isEmpty()) {
            insertBatch(batch);
        }
    }

    @PreDestroy
    public void flushBeforeShutdown() {
        List<ChatHistoryRecord> batch = new ArrayList<>(queue.size());
        queue.drainTo(batch);
        if (!batch.isEmpty()) {
            insertBatch(batch);
        }
    }

    private void insertBatch(List<ChatHistoryRecord> batch) {
        try {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO agent_chat_history (conversation_id, user_question, assistant_answer, model_name, total_duration_ms, tokens_input, tokens_output, status, error_message) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    batch,
                    batch.size(),
                    (ps, record) -> {
                        ps.setString(1, record.conversationId());
                        ps.setString(2, record.question());
                        ps.setString(3, record.answer());
                        ps.setString(4, record.model());
                        ps.setInt(5, record.durationMs());
                        ps.setInt(6, record.tokensInput());
                        ps.setInt(7, record.tokensOutput());
                        ps.setString(8, record.status());
                        ps.setString(9, record.errorMsg());
                    }
            );
        } catch (Exception e) {
            log.warn("批量保存聊天历史失败: batchSize={}, error={}", batch.size(), e.getMessage());
        }
    }

    private record ChatHistoryRecord(
            String conversationId,
            String question,
            String answer,
            String model,
            int durationMs,
            int tokensInput,
            int tokensOutput,
            String status,
            String errorMsg
    ) {
    }
}
