package com.leantech.agent.service;

import com.leantech.agent.async.AsyncChatLogQueue;
import com.leantech.agent.doc.ChatMessageDoc;
import com.leantech.agent.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMemoryService {

    private final AsyncChatLogQueue asyncQueue;
    private final ChatMessageRepository repository; // 用于读取

    public ChatMemoryService(AsyncChatLogQueue asyncQueue, ChatMessageRepository repository) {
        this.asyncQueue = asyncQueue;
        this.repository = repository;
    }

    public void saveMessage(String userId, String conversationId, String role, String content) {
        ChatMessageDoc doc = new ChatMessageDoc();
        doc.setUserId(userId);
        doc.setConversationId(conversationId);
        doc.setRole(role);
        doc.setContent(content);
        doc.setTimestamp(LocalDateTime.now());
        // 放入队列异步批量写入
        asyncQueue.offer(doc);
    }

    public List<ChatMessageDoc> getRecentHistory(String userId) {
        // 仍然直接读取（读取走同步保证实时性）
        return repository.findTop20ByUserIdOrderByTimestampAsc(userId);
    }
}