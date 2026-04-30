package  com.leantech.agent.service;
import com.leantech.agent.model.LogQueryResponse;
import com.leantech.agent.model.LogSummaryVO;
import com.leantech.agent.annotation.ChatLog;
import com.leantech.agent.model.QueryRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AgentService extends BaseService {

    private final ChatClient chatClient;
    private final QueryService queryService;
    private final ChatMemoryService chatMemoryService;

    public AgentService(ChatClient chatClient,
                        QueryService queryService,
                        ChatMemoryService chatMemoryService) {
        this.chatClient = chatClient;
        this.queryService = queryService;
        this.chatMemoryService = chatMemoryService;
    }

    @ChatLog(saveMemory = true, saveHistory = true)
    public String chat(String message, String userId) {
        String historyContext = buildHistoryContext(chatMemoryService, userId);
        return chatClient.prompt()
                .user(historyContext + "用户: " + message)
                .call()
                .content();
    }

    @ChatLog(saveMemory = true, saveHistory = true)
    public Flux<String> chatStream(String message, String userId) {
        // 流式场景下，切面只能记录最终的完整回答，中间片段不会单独保存
        String historyContext = buildHistoryContext(chatMemoryService, userId);
        return chatClient.prompt()
                .user(historyContext + "用户: " + message)
                .stream()
                .content();
    }

    public String executeQuery(QueryRequest request) {
        return queryService.executeQuery(request);
    }

    public LogQueryResponse.PageData<LogSummaryVO> executeStructuredQuery(QueryRequest request) {
        return queryService.executeStructuredQuery(request);
    }
}
