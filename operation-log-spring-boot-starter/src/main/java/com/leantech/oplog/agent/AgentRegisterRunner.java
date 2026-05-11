package com.leantech.oplog.agent;

import com.leantech.oplog.config.AgentRegisterProperties;
import com.leantech.oplog.config.OperationLogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class AgentRegisterRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentRegisterRunner.class);

    private final AgentRegisterProperties properties;
    private final OperationLogProperties opLogProperties;
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final Executor executor;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${th.organizationcode:}")
    private String thOrgCode;

    @Value("${th.organizationname:}")
    private String thOrgName;

    public AgentRegisterRunner(AgentRegisterProperties properties,
                               OperationLogProperties opLogProperties,
                               JdbcTemplate jdbcTemplate,
                               RestTemplate restTemplate,
                               @Qualifier("operationLogAsyncExecutor") Executor executor) {
        this.properties = properties;
        this.opLogProperties = opLogProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
        this.executor = executor;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("自动注册已禁用");
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                doRegister();
            }
        });
    }

    private void doRegister() {
        try {
            String orgCode = resolveOrgCode();
            String orgName = resolveOrgName();
            String baseUrl = resolveBaseUrl();
            String apiKey = resolveApiKey();

            List<Map<String, Object>> bizTypes = jdbcTemplate.queryForList(
                    "SELECT type_code, type_name, keywords, sort_order FROM operation_biz_type_dict WHERE is_enabled = 1 ORDER BY sort_order");
            List<Map<String, Object>> serviceTypes = jdbcTemplate.queryForList(
                    "SELECT type_code, type_name, class_keywords, sort_order FROM operation_service_type_dict WHERE is_enabled = 1 ORDER BY sort_order");

            Map<String, Object> request = new HashMap<String, Object>();
            request.put("orgCode", orgCode);
            request.put("orgName", orgName);
            request.put("baseUrl", baseUrl);
            request.put("apiKey", apiKey);

            List<Map<String, Object>> bizList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> row : bizTypes) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("typeCode", row.get("type_code"));
                item.put("typeName", row.get("type_name"));
                item.put("keywords", row.get("keywords"));
                item.put("sortOrder", row.get("sort_order"));
                bizList.add(item);
            }
            request.put("bizTypes", bizList);

            List<Map<String, Object>> svcList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> row : serviceTypes) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("typeCode", row.get("type_code"));
                item.put("typeName", row.get("type_name"));
                item.put("classKeywords", row.get("class_keywords"));
                item.put("sortOrder", row.get("sort_order"));
                svcList.add(item);
            }
            request.put("serviceTypes", svcList);

            String agentUrl = properties.getAgentUrl();
            if (!StringUtils.hasText(agentUrl)) {
                log.warn("未配置 agent 注册地址，跳过注册");
                return;
            }

            long now = System.currentTimeMillis();
            String sign = DigestUtils.md5DigestAsHex(
                    (apiKey + orgCode + now).getBytes(StandardCharsets.UTF_8)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Sign", sign);
            headers.set("X-API-Timestamp", String.valueOf(now));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(request, headers);

            String url = agentUrl + "/agent-admin-server/admin/hospital/register";
            log.info("正在向中心注册：{}，机构代码：{}", url, orgCode);
            String resp = restTemplate.postForObject(url, entity, String.class);
            log.info("注册结果：{}", resp);
        } catch (Exception e) {
            log.error("自动注册失败，不影响业务：{}", e.getMessage());
        }
    }

    private String resolveOrgCode() {
        if (StringUtils.hasText(properties.getOrgCode())) {
            return properties.getOrgCode();
        }
        if (StringUtils.hasText(opLogProperties.getUserInfo().getFixedOrgCode())) {
            return opLogProperties.getUserInfo().getFixedOrgCode();
        }
        if (StringUtils.hasText(thOrgCode)) {
            return thOrgCode;
        }
        return "UNKNOWN";
    }

    private String resolveOrgName() {
        if (StringUtils.hasText(thOrgName)) {
            return thOrgName;
        }
        if (StringUtils.hasText(properties.getOrgName())) {
            return properties.getOrgName();
        }
        return resolveOrgCode();
    }

    private String resolveBaseUrl() {
        if (properties.getBaseUrl() != null && !properties.getBaseUrl().isEmpty()) {
            return properties.getBaseUrl();
        }
        return "http://" + serverAddress + ":" + serverPort + contextPath;
    }

    private String resolveApiKey() {
        if (StringUtils.hasText(properties.getApiKey())) {
            return properties.getApiKey();
        }
        return opLogProperties.getApi().getApiKey();
    }
}
