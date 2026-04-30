package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.service.HospitalHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class HospitalHeartbeatController extends BaseController {

    private final HospitalHealthService healthService;

    public HospitalHeartbeatController(HospitalHealthService healthService) {
        this.healthService = healthService;
    }

    @PostMapping("/hospital/heartbeat")
    public ApiResult<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> request,
                                                    @RequestHeader(value = "X-API-Sign", required = false) String sign,
                                                    @RequestHeader(value = "X-API-Timestamp", required = false) String timestampStr) {
        String orgCode = (String) request.get("orgCode");

        // 统一签名校验
        ApiResult<Map<String, Object>> signError = validateHospitalSign(orgCode, sign, timestampStr);
        if (signError != null) return signError;

        healthService.updateHeartbeat(orgCode);
        return ok(Map.of(
                "orgCode", orgCode,
                "forceUpgrade", false
        ));
    }
}