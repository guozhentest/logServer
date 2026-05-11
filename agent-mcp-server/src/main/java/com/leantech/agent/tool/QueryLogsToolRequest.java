package com.leantech.agent.tool;

public record QueryLogsToolRequest(
        String orgCode,
        String userId,
        String loginId,
        String bizTypeCode,
        String serviceType,
        String responseStatus,
        String startTime,
        String endTime,
        String traceIdPrefix,
        String orderNo
) {
}
