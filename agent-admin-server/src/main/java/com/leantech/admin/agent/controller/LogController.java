package com.leantech.admin.agent.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leantech.admin.agent.entity.HospitalInfoEntity;
import com.leantech.admin.agent.service.HospitalService;
import com.leantech.admin.agent.service.LogDictService;
import com.leantech.admin.agent.util.SignUtil;
import com.leantech.admin.common.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/agent/log")
@RequiredArgsConstructor
public class LogController extends BaseController {

    private final HospitalService hospitalService;
    private final LogDictService logDictService;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/dict/{orgCode}")
    public R<Map<String, Object>> dict(@PathVariable String orgCode) {
        return R.ok(logDictService.getDict(orgCode));
    }

    @PostMapping("/query")
    public R<Map<String, Object>> query(@RequestBody LogQueryParam param) {
        HospitalInfoEntity hospital = findHospital(param.getOrgCode());
        if (hospital == null) {
            return R.fail("未找到机构: " + param.getOrgCode());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orgCode", param.getOrgCode());
        body.put("userId", param.getUserId());
        body.put("bizTypeCode", param.getBizTypeCode());
        body.put("serviceType", param.getServiceType());
        body.put("responseStatus", param.getResponseStatus());
        body.put("orderNo", param.getOrderNo());
        body.put("startTime", param.getStartTime());
        body.put("endTime", param.getEndTime());
        body.put("traceIdPrefix", param.getTraceIdPrefix());
        body.put("page", param.getPageNum() != null ? param.getPageNum() : 1);
        body.put("size", param.getPageSize() != null ? param.getPageSize() : 20);

        String url = hospital.getBaseUrl() + "/common/operation-log/query";
        try {
            HttpEntity<Map<String, Object>> entity = buildEntity(hospital, body);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JSONObject resultJson = JSON.parseObject(response.getBody());

            Map<String, Object> result = new HashMap<>();
            JSONObject data = resultJson.getJSONObject("data");
            if (data != null) {
                result.put("total", data.getLongValue("total"));
                result.put("rows", data.getJSONArray("records"));
            } else {
                result.put("total", 0);
                result.put("rows", Collections.emptyList());
            }
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询日志失败 url={} orgCode={}", url, param.getOrgCode(), e);
            return R.fail("查询日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{logId}")
    public R<Map<String, Object>> detail(@PathVariable String logId, @RequestParam String orgCode) {
        HospitalInfoEntity hospital = findHospital(orgCode);
        if (hospital == null) {
            return R.fail("未找到机构: " + orgCode);
        }

        String url = hospital.getBaseUrl() + "/common/operation-log/detail/" + logId + "?orgCode=" + orgCode;
        try {
            HttpEntity<Void> entity = buildEmptyEntity(hospital);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JSONObject resultJson = JSON.parseObject(response.getBody());
            return R.ok(resultJson.getJSONObject("data"));
        } catch (Exception e) {
            log.error("查询日志详情失败 url={} logId={} orgCode={}", url, logId, orgCode, e);
            return R.fail("查询日志详情失败: " + e.getMessage());
        }
    }

    private HospitalInfoEntity findHospital(String orgCode) {
        return hospitalService.listAllDetail().stream()
                .filter(h -> orgCode.equals(h.getOrgCode()))
                .findFirst().orElse(null);
    }

    private HttpEntity<Map<String, Object>> buildEntity(HospitalInfoEntity hospital, Map<String, Object> body) {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(hospital.getApiKey(), hospital.getOrgCode(), timestamp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Sign", sign);
        headers.set("X-API-Timestamp", String.valueOf(timestamp));
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> buildEmptyEntity(HospitalInfoEntity hospital) {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(hospital.getApiKey(), hospital.getOrgCode(), timestamp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Sign", sign);
        headers.set("X-API-Timestamp", String.valueOf(timestamp));
        return new HttpEntity<>(headers);
    }

    @Data
    public static class LogQueryParam {
        private String orgCode;
        private String userId;
        private String bizTypeCode;
        private String serviceType;
        private String responseStatus;
        private String orderNo;
        private String startTime;
        private String endTime;
        private String traceIdPrefix;
        private Integer pageNum;
        private Integer pageSize;
    }
}
