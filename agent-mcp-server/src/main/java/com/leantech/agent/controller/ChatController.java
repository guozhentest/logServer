package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.model.LogQueryResponse;
import com.leantech.agent.model.LogSummaryVO;
import com.leantech.agent.model.QueryRequest;
import com.leantech.agent.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/agent")
public class ChatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ApiResult<String> chat(@RequestBody String message) {
        String userId = getCurrentUserId();
        log.info("用户 [{}] 发起查询: {}", userId, message);
        String answer = agentService.chat(message, userId);
        return ok(answer);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody String message) {
        String userId = getCurrentUserId();
        log.info("用户 [{}] 发起流式查询: {}", userId, message);
        return agentService.chatStream(message, userId);
    }

    @PostMapping("/query-direct")
    public ApiResult<LogQueryResponse.PageData<LogSummaryVO>> queryDirect(@RequestBody QueryRequest request) {
        try {
            LogQueryResponse.PageData<LogSummaryVO> result = agentService.executeStructuredQuery(request);
            return ok(result);
        } catch (Exception e) {
            return fail("查询失败：" + e.getMessage());
        }
    }
}
