package com.leantech.oplog.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class AgentHeartbeatReporter {

    private static final Logger log = LoggerFactory.getLogger(AgentHeartbeatReporter.class);

    @Value("${operation.log.agent.register.agent-url:}")
    private String agentUrl;

    @Value("${operation.log.api.api-key:}")
    private String apiKey;

    @Value("${operation.log.agent.register.org-code:${th.organizationcode:}}")
    private String orgCode;

    @Value("${operation.log.agent.register.org-name:${th.organizationname:}}")
    private String orgName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedDelay = 30_000) // 每30秒上报一次
    public void sendHeartbeat() {
        if (agentUrl == null || agentUrl.isEmpty()) return;

        try {
            long now = System.currentTimeMillis();
            String sign = DigestUtils.md5DigestAsHex(
                    (apiKey + orgCode + now).getBytes(StandardCharsets.UTF_8)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Sign", sign);
            headers.set("X-API-Timestamp", String.valueOf(now));
            headers.setContentType(MediaType.APPLICATION_JSON);

            // JDK 8 兼容写法：使用 HashMap 代替 Map.of
            Map<String, Object> body = new HashMap<>();
            body.put("orgCode", orgCode);
            body.put("orgName", orgName);
            body.put("timestamp", now);
            body.put("version", "2.0.0");

            String url = agentUrl + "/agent/hospital/heartbeat";
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("心跳上报成功: {}", orgCode);
        } catch (Exception e) {
            log.warn("心跳上报失败: {}", e.getMessage());
        }
    }

}