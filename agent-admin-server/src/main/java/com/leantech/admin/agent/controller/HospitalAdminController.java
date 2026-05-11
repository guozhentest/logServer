package com.leantech.admin.agent.controller;

import com.leantech.admin.agent.service.HospitalRegistrationService;
import com.leantech.admin.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hospital")
@RequiredArgsConstructor
public class HospitalAdminController extends BaseController {

    private final HospitalRegistrationService registrationService;

    @PostMapping("/register")
    public R<Map<String, String>> register(@RequestBody Map<String, Object> request) {
        String orgCode = (String) request.get("orgCode");
        String orgName = (String) request.get("orgName");
        String baseUrl = (String) request.get("baseUrl");
        String apiKey = (String) request.get("apiKey");

        if (orgCode == null || orgName == null || baseUrl == null) {
            return R.fail("缺少必填参数");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> bizTypes = (List<Map<String, String>>) request.get("bizTypes");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> serviceTypes = (List<Map<String, String>>) request.get("serviceTypes");

        registrationService.register(orgCode, orgName, baseUrl, apiKey, bizTypes, serviceTypes);
        return R.ok();
    }
}
