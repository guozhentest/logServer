package com.leantech.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.leantech.agent.model.LogQueryRequest;
import com.leantech.agent.model.LogQueryResponse;
import com.leantech.agent.model.LogSummaryVO;
import com.leantech.agent.model.QueryRequest;
import com.leantech.agent.service.AgentTelemetryService;
import com.leantech.agent.service.HospitalHealthService;
import com.leantech.agent.service.HospitalRouteService;
import com.leantech.agent.util.SignUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
public class LogQueryTool {

    private final HospitalRouteService routeService;
    private final RestTemplate restTemplate;
    private final AgentTelemetryService telemetryService;
    private final HospitalHealthService healthService;
    public LogQueryTool(HospitalRouteService routeService, RestTemplate restTemplate,
                        AgentTelemetryService telemetryService,HospitalHealthService healthService) {
        this.routeService = routeService;
        this.restTemplate = restTemplate;
        this.telemetryService = telemetryService;
        this.healthService = healthService;
    }

    @Tool(description = "Query hospital operation logs. Extract parameters from user input: orgCode (required, long number string), userId (empty if not specified), bizTypeCode (REG/PAY/ONLINE/DRUG/EXA/INP), responseStatus (SUCCESS/FAILURE), orderNo (optional), startTime (yyyy-MM-dd HH:mm:ss), endTime (yyyy-MM-dd HH:mm:ss). Do not ask the user for missing parameters.")
    public String queryLogs(
            @ToolParam(description = "机构代码") String orgCode,
            @ToolParam(description = "用户工号，没有则留空") String userId,
            @ToolParam(description = "登录会话ID，没有则留空") String loginId,
            @ToolParam(description = "业务类型编码，如 REG") String bizTypeCode,
            @ToolParam(description = "服务类型，可选") String serviceType,
            @ToolParam(description = "响应状态 SUCCESS/FAILURE") String responseStatus,
            @ToolParam(description = "开始时间 yyyy-MM-dd HH:mm:ss 或 today") String startTimeStr,
            @ToolParam(description = "结束时间 yyyy-MM-dd HH:mm:ss 或 now") String endTimeStr,
            @ToolParam(description = "TraceID 前缀，可选") String traceIdPrefix,
            @ToolParam(description = "订单号，可选") String orderNo) {
        LogQueryResponse.PageData<LogSummaryVO> data = doQuery(
                orgCode,
                userId,
                loginId,
                bizTypeCode,
                serviceType,
                responseStatus,
                startTimeStr,
                endTimeStr,
                traceIdPrefix,
                orderNo,
                1,
                50
        );
        // 健康检查
        if (!healthService.isOnline(orgCode)) {
            return "医院 [ " + orgCode + " ] 当前离线或不可用，请稍后重试。";
        }
        return formatResult(data);
    }

    public LogQueryResponse.PageData<LogSummaryVO> queryLogsPage(QueryRequest request) {
        return doQuery(
                request.getOrgCode(),
                request.getUserId(),
                "",
                request.getBizTypeCode(),
                request.getServiceType(),
                request.getResponseStatus(),
                request.getStartTime(),
                request.getEndTime(),
                request.getTraceIdPrefix(),
                request.getOrderNo(),
                request.getPage(),
                request.getSize()
        );
    }

    private LogQueryResponse.PageData<LogSummaryVO> doQuery(
            String orgCode,
            String userId,
            String loginId,
            String bizTypeCode,
            String serviceType,
            String responseStatus,
            String startTimeStr,
            String endTimeStr,
            String traceIdPrefix,
            String orderNo,
            Integer page,
            Integer size) {
        String baseUrl = routeService.getHospitalUrl(orgCode);
        String apiKey = routeService.getHospitalApiKey(orgCode);
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("未找到机构 [" + orgCode + "] 的接口配置，请联系管理员。");
        }
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("机构 [" + orgCode + "] 未配置apiKey，请联系管理员。");
        }

        LogQueryRequest request = new LogQueryRequest();
        request.setOrgCode(orgCode);
        request.setUserId(userId);
        request.setLoginId(loginId);
        request.setBizTypeCode(bizTypeCode);
        request.setServiceType(serviceType);
        request.setResponseStatus(responseStatus);
        request.setTraceIdPrefix(traceIdPrefix);
        request.setOrderNo(orderNo);
        request.setPage(page != null && page > 0 ? page : 1);
        request.setSize(size != null && size > 0 ? size : 10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        request.setStartTime(parseDateTime(startTimeStr, formatter, LocalDateTime.now().minusDays(7)));
        request.setEndTime(parseDateTime(endTimeStr, formatter, LocalDateTime.now()));

        String url = baseUrl + "/common/operation-log/query";

        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(apiKey, orgCode, timestamp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Sign", sign);
        headers.set("X-API-Timestamp", String.valueOf(timestamp));
        HttpEntity<LogQueryRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.info("查询日志url:{},入参:{}", url, JSON.toJSONString(entity));

            // ✅ 使用 ParameterizedTypeReference 解决泛型嵌套反序列化问题
            ResponseEntity<LogQueryResponse<LogSummaryVO>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    });

            LogQueryResponse<LogSummaryVO> response = responseEntity.getBody();
            log.info("查询日志出参:{}", JSON.toJSONString(response));

            if (response != null && Integer.valueOf(0).equals(response.getCode()) && response.getData() != null) {
                return response.getData();
            }
            throw new IllegalStateException("查询失败：" + (response != null ? response.getMessage() : "未知错误"));
        } catch (Exception e) {
            log.error("调用医院接口异常", e);
            throw new IllegalStateException("调用医院接口异常：" + e.getMessage(), e);
        }
    }

    private String formatResult(LogQueryResponse.PageData<LogSummaryVO> data) {
        if (data == null) {
            return "未查询到相关日志记录。";
        }
        List<LogSummaryVO> records = data.getRecords();
        if (records == null || records.isEmpty()) {
            return "未查询到相关日志记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共找到 ").append(data.getTotal()).append(" 条日志，当前显示前 ")
                .append(records.size()).append(" 条：\n\n");

        for (int i = 0; i < records.size(); i++) {
            LogSummaryVO log = records.get(i);
            sb.append(String.format("[%d] %s | %s | %s | %s | %s | 耗时 %dms | 状态: %s%n",
                    i + 1,
                    log.getCreatedAt() != null ? log.getCreatedAt() : "",
                    log.getOrderNo() != null ? "订单:" + log.getOrderNo() : "-",
                    log.getBizTypeCode() != null ? log.getBizTypeCode() : "-",
                    log.getOperation() != null ? log.getOperation() : "-",
                    log.getApiName() != null ? log.getApiName() : "-",
                    log.getCostMs() != null ? log.getCostMs() : 0,
                    log.getResponseStatus() != null ? log.getResponseStatus() : "-"
            ));
            if (Boolean.TRUE.equals(log.getHasDetail())) {
                sb.append("   请求预览: ").append(truncate(log.getRequestBodyPreview(), 1000)).append("\n");
                sb.append("   响应预览: ").append(log.getResponseBodyPreview()).append("\n");
            }
            sb.append("\n");
        }

        if (data.getTotal() != null && data.getTotal() > records.size()) {
            sb.append("提示：日志量较大，请缩小查询范围获取更精确结果。");
        }
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    private LocalDateTime parseDateTime(String value, DateTimeFormatter formatter, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }
    }
}
