package com.leantech.agent.controller;

import com.alibaba.fastjson2.JSON;
import com.leantech.agent.model.FeedbackRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class FeedbackController {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/feedback")
    public Map<String, Object> submitFeedback(@RequestBody FeedbackRequest request) {
        jdbcTemplate.update(
                "INSERT INTO agent_feedback (conversation_id, user_question, assistant_answer, rating, tags, comment) VALUES (?, ?, ?, ?, ?, ?)",
                request.getConversationId(),
                request.getQuestion(),
                request.getAnswer(),
                request.getRating(),
                request.getTags() != null ? JSON.toJSONString(request.getTags()) : null,
                request.getComment()
        );
        return Map.of("code", 0, "message", "感谢您的反馈");
    }
}
