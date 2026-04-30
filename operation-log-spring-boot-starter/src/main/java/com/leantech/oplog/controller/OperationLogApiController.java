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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

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
        // 1. API Key 必填校验
        String apiKey = properties.getApi().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            return buildErrorResponse(500, "服务端未配置API密钥，请联系管理员");
        }

        // 2. 签名参数校验
        String sign = httpRequest.getHeader(SIGN_HEADER);
        String timestampStr = httpRequest.getHeader(TIMESTAMP_HEADER);
        if (!StringUtils.hasText(sign) || !StringUtils.hasText(timestampStr)) {
            return buildErrorResponse(401, "缺少签名参数 X-API-Sign 或 X-API-Timestamp");
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException ex) {
            return buildErrorResponse(400, "时间戳格式错误");
        }

        // 3. 时效校验（5分钟）
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > SIGN_EXPIRE_MILLIS) {
            return buildErrorResponse(403, "请求已过期，时间戳超过5分钟");
        }

        // 4. 获取请求中的 orgCode
        String requestOrgCode = request.getOrgCode();
        if (!StringUtils.hasText(requestOrgCode)) {
            requestOrgCode = httpRequest.getHeader(ORG_CODE_HEADER);
        }
        if (!StringUtils.hasText(requestOrgCode)) {
            return buildErrorResponse(400, "无法获取机构代码 orgCode");
        }

        // 5. 签名校验
        String raw = apiKey + requestOrgCode + timestamp;
        String expectedSign = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        if (!expectedSign.equalsIgnoreCase(sign)) {
            return buildErrorResponse(403, "签名验证失败");
        }

        // 6. 机构代码一致性校验（防止跨机构查询）
        String localOrgCode = properties.getUserInfo().getFixedOrgCode();
        if (!StringUtils.hasText(localOrgCode)) {
            localOrgCode = defaultOrgCode;
        }
        if (!StringUtils.hasText(localOrgCode)) {
            return buildErrorResponse(500, "服务端未配置机构代码");
        }
        if (!localOrgCode.equals(requestOrgCode)) {
            return buildErrorResponse(403, "无权查询其他机构的日志");
        }

        // 7. 执行查询
        try {
            LogQueryResponse.PageData<LogSummaryVO> pageData = operationLogService.queryLogs(
                    request, properties.getApi().getMaxLimit(), properties.getApi().getBodyTruncateLength());
            LogQueryResponse<LogSummaryVO> resp = new LogQueryResponse<>();
            resp.setCode(0);
            resp.setMessage("success");
            resp.setData(pageData);
            return resp;
        } catch (Exception ex) {
            log.error("查询失败:", ex);
            return buildErrorResponse(500, "查询失败：" + ex.getMessage());
        }
    }

    private LogQueryResponse<LogSummaryVO> buildErrorResponse(int code, String message) {
        LogQueryResponse<LogSummaryVO> resp = new LogQueryResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }
}
