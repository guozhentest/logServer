package com.leantech.agent.service;

import com.leantech.agent.model.LogQueryResponse;
import com.leantech.agent.model.LogSummaryVO;
import com.leantech.agent.model.QueryRequest;
import com.leantech.agent.tool.LogQueryTool;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    private final LogQueryTool logQueryTool;

    public QueryService(LogQueryTool logQueryTool) {
        this.logQueryTool = logQueryTool;
    }

    /**
     * 执行直接条件查询，返回日志结果字符串
     */
    public String executeQuery(QueryRequest request) {
        return logQueryTool.queryLogs(
                request.getOrgCode(),
                request.getUserId(),
                "",   // loginId 暂时不需要，传空字符串
                request.getBizTypeCode(),
                request.getServiceType(),
                request.getResponseStatus(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTraceIdPrefix()
        );
    }

    public LogQueryResponse.PageData<LogSummaryVO> executeStructuredQuery(QueryRequest request) {
        return logQueryTool.queryLogsPage(request);
    }
}
