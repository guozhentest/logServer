package com.leantech.agent.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "chat_history")
public class ChatMessageDoc {
    @Id
    private String id;
    private String userId;          // 登录用户名
    private String conversationId;  // 会话ID（同一轮对话）
    private String role;            // USER / ASSISTANT
    private String content;         // 消息内容
    private LocalDateTime timestamp;
}