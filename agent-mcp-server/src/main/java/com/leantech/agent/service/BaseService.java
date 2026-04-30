package com.leantech.agent.service;

import com.leantech.agent.doc.ChatMessageDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 构建用户历史对话上下文（用于PS prompt）
     */
    protected String buildHistoryContext(ChatMemoryService memoryService, String userId) {
        List<ChatMessageDoc> history = memoryService.getRecentHistory(userId);
        if (history.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("以下是该用户的历史对话，请参考以提供更连贯的回答：\n");
        for (ChatMessageDoc msg : history) {
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }
}