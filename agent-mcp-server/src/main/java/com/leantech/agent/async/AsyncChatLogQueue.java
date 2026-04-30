package com.leantech.agent.async;

import com.leantech.agent.doc.ChatMessageDoc;
import com.leantech.agent.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;   // 正确导入
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AsyncChatLogQueue {

    private static final Logger log = LoggerFactory.getLogger(AsyncChatLogQueue.class);
    private final BlockingQueue<ChatMessageDoc> queue = new LinkedBlockingQueue<>(5000);
    private final ChatMessageRepository repository;

    public AsyncChatLogQueue(ChatMessageRepository repository) {
        this.repository = repository;
    }

    /**
     * 对外提交入口：异步保存单条消息
     */
    public void offer(ChatMessageDoc doc) {
        if (!queue.offer(doc)) {
            log.warn("聊天记录队列已满，丢弃 userId={}, conversationId={}", doc.getUserId(), doc.getConversationId());
        }
    }

    /**
     * 定时批量写入（每 2 秒或满 100 条立即写入）
     */
    @Scheduled(fixedDelay = 2000)
    public void flush() {
        List<ChatMessageDoc> batch = new ArrayList<>();
        queue.drainTo(batch, 100);
        if (!batch.isEmpty()) {
            try {
                repository.saveAll(batch);
                log.debug("批量写入聊天记录 {} 条", batch.size());
            } catch (Exception e) {
                log.error("批量写入聊天记录失败", e);
                // 失败后重新放回队列（避免丢失）
                for (ChatMessageDoc doc : batch) {
                    queue.offer(doc);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        List<ChatMessageDoc> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            repository.saveAll(remaining);
            log.info("关闭时写入剩余聊天记录 {} 条", remaining.size());
        }
    }
}