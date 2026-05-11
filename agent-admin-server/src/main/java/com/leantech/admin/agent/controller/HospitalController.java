package com.leantech.admin.agent.controller;

import com.leantech.admin.agent.entity.HospitalInfoEntity;
import com.leantech.admin.agent.service.HospitalHealthService;
import com.leantech.admin.agent.service.HospitalService;
import com.leantech.admin.agent.service.SseEmitterService;
import com.leantech.admin.agent.util.SignValidator;
import com.leantech.admin.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/hospital")
@RequiredArgsConstructor
public class HospitalController extends BaseController {

    private final HospitalService service;
    private final HospitalHealthService healthService;
    private final SseEmitterService sseEmitterService;

    @GetMapping(value = "/status-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter statusStream() {
        return sseEmitterService.subscribe();
    }

    @PostMapping("/heartbeat")
    public R<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> request,
                                            @RequestHeader(value = "X-API-Sign", required = false) String sign,
                                            @RequestHeader(value = "X-API-Timestamp", required = false) String timestampStr) {
        String orgCode = (String) request.get("orgCode");
        if (orgCode == null || orgCode.isEmpty()) {
            return R.fail("缺少机构代码");
        }
        HospitalInfoEntity hospital = service.getByOrgCode(orgCode);
        if (hospital == null || hospital.getApiKey() == null) {
            return R.fail("机构未配置API密钥");
        }
        if (!SignValidator.validate(hospital.getApiKey(), orgCode, sign, timestampStr)) {
            return R.fail("签名验证失败");
        }
        healthService.updateHeartbeat(orgCode);
        sseEmitterService.notifyHeartbeat(orgCode);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orgCode", orgCode);
        data.put("forceUpgrade", false);
        return R.ok(data);
    }

    @GetMapping("/list")
    public TableDataInfo<Map<String, Object>> list() {
        List<HospitalInfoEntity> all = service.listAllDetail();
        List<Map<String, Object>> rows = all.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("orgCode", h.getOrgCode());
            m.put("orgName", h.getOrgName());
            m.put("baseUrl", h.getBaseUrl());
            m.put("apiKey", h.getApiKey());
            m.put("status", h.getStatus());
            m.put("online", healthService.isOnline(h.getOrgCode()));
            Long hb = healthService.getLastHeartbeatTime(h.getOrgCode());
            m.put("lastHeartbeatTime", hb);
            m.put("createTime", h.getCreateTime());
            m.put("updateTime", h.getUpdateTime());
            return m;
        }).toList();
        return TableDataInfo.build(rows, rows.size());
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> getInfo(@PathVariable Long id) {
        HospitalInfoEntity h = service.getById(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("orgCode", h.getOrgCode());
        m.put("orgName", h.getOrgName());
        m.put("baseUrl", h.getBaseUrl());
        m.put("apiKey", h.getApiKey());
        m.put("status", h.getStatus());
        return R.ok(m);
    }

    @PutMapping("/{id}")
    public R<Void> edit(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        service.update(id, data);
        return R.ok();
    }
}
