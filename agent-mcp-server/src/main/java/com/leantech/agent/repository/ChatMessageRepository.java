package com.leantech.agent.repository;

import com.leantech.agent.doc.ChatMessageDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDoc, String> {

    // 根据用户ID查询最近的消息，按时间升序
    List<ChatMessageDoc> findTop20ByUserIdOrderByTimestampAsc(String userId);
}