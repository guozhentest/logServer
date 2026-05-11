package com.leantech.oplog.controller;

import com.alibaba.fastjson2.JSONObject;
import com.leantech.oplog.config.OperationLogProperties;
import com.leantech.oplog.model.LogQueryRequest;
import com.leantech.oplog.model.LogQueryResponse;
import com.leantech.oplog.model.LogSummaryVO;
import com.leantech.oplog.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/common/operation-log")
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "operation.log.api", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OperationLogApiController {

    private static final String SIGN_HEADER = "X-API-Sign";
    private static final String TIMESTAMP_HEADER = "X-API-Timestamp";
    private static final String ORG_CODE_HEADER = "X-Org-Code";
    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000L;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogProperties properties;

    @Value("${th.organizationcode:}")
    private String defaultOrgCode;

    @PostMapping("/query")
    public LogQueryResponse<LogSummaryVO> query(@RequestBody LogQueryRequest request, HttpServletRequest httpRequest) {
        log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), JSONObject.toJSONString(request));
        String orgCode = validateAndGetOrgCode(httpRequest, request.getOrgCode());
        if (orgCode == null) return buildErrorResponse(400, "无法获取机构代码 orgCode");

        try {
            LogQueryResponse.PageData<LogSummaryVO> pageData = operationLogService.queryLogs(
                    request, properties.getApi().getMaxLimit(), properties.getApi().getBodyTruncateLength());
            LogQueryResponse<LogSummaryVO> resp = new LogQueryResponse<>();
            resp.setCode(0);
            resp.setMessage("success");
            resp.setData(pageData);
            log.info("出参:{}", JSONObject.toJSONString(resp));
            return resp;
        } catch (Exception ex) {
            log.error("查询失败:", ex);
            return buildErrorResponse(500, "查询失败：" + ex.getMessage());
        }
    }

    @GetMapping("/detail/{logId}")
    public Map<String, Object> detail(@PathVariable String logId,
                                       @RequestParam String orgCode,
                                       HttpServletRequest httpRequest) {
        log.info("{} {} logId={} orgCode={}", httpRequest.getMethod(), httpRequest.getRequestURI(), logId, orgCode);
        String validOrgCode = validateAndGetOrgCode(httpRequest, orgCode);
        if (validOrgCode == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 400);
            err.put("message", "无法获取机构代码 orgCode");
            return err;
        }

        try {
            Map<String, Object> detail = operationLogService.queryDetail(logId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("message", "success");
            resp.put("data", detail);
            log.info("出参:{}", JSONObject.toJSONString(resp));
            return resp;
        } catch (Exception ex) {
            log.error("查询详情失败:", ex);
            Map<String, Object> err = new HashMap<>();
            err.put("code", 500);
            err.put("message", "查询失败：" + ex.getMessage());
            return err;
        }
    }

    private String validateAndGetOrgCode(HttpServletRequest httpRequest, String bodyOrgCode) {
        // 1. API Key 必填校验
        String apiKey = properties.getApi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            return null;
        }

        // 2. 签名参数校验
        String sign = httpRequest.getHeader(SIGN_HEADER);
        String timestampStr = httpRequest.getHeader(TIMESTAMP_HEADER);
        if (!StringUtils.hasText(sign) || !StringUtils.hasText(timestampStr)) {
            return null;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException ex) {
            return null;
        }

        // 3. 时效校验（5分钟）
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > SIGN_EXPIRE_MILLIS) {
            return null;
        }

        // 4. 获取请求中的 orgCode
        String requestOrgCode = bodyOrgCode;
        if (!StringUtils.hasText(requestOrgCode)) {
            requestOrgCode = httpRequest.getHeader(ORG_CODE_HEADER);
        }
        if (!StringUtils.hasText(requestOrgCode)) {
            return null;
        }

        // 5. 签名校验
        String raw = apiKey + requestOrgCode + timestamp;
        String expectedSign = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        if (!expectedSign.equalsIgnoreCase(sign)) {
            return null;
        }

        // 6. 机构代码一致性校验
        String localOrgCode = properties.getUserInfo().getFixedOrgCode();
        if (!StringUtils.hasText(localOrgCode)) {
            localOrgCode = defaultOrgCode;
        }
        if (!StringUtils.hasText(localOrgCode)) {
            return null;
        }
        if (!localOrgCode.equals(requestOrgCode)) {
            return null;
        }

        return requestOrgCode;
    }

    private LogQueryResponse<LogSummaryVO> buildErrorResponse(int code, String message) {
        LogQueryResponse<LogSummaryVO> resp = new LogQueryResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }
}
