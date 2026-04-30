package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.model.HospitalRegisterRequest;
import com.leantech.agent.service.HospitalRegistrationService;
import com.leantech.agent.service.HospitalRouteService;
import com.leantech.agent.util.SignValidator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class HospitalRegisterController extends BaseController {

    private final HospitalRegistrationService registrationService;
    private final HospitalRouteService routeService;

    public HospitalRegisterController(HospitalRegistrationService registrationService,
                                      HospitalRouteService routeService) {
        this.registrationService = registrationService;
        this.routeService = routeService;
    }

    @PostMapping("/hospital/register")
    public ApiResult<String> register(@RequestBody HospitalRegisterRequest request,
                                      @RequestHeader(value = "X-API-Sign", required = false) String sign,
                                      @RequestHeader(value = "X-API-Timestamp", required = false) String timestampStr) {
        String orgCode = request.getOrgCode();

        // 格式校验：只检查签名头是否完整、时间戳是否在有效期内，不验证 Key 是否正确
        if (sign == null || sign.isEmpty() || timestampStr == null || timestampStr.isEmpty()) {
            return fail(401, "缺少签名参数 X-API-Sign 或 X-API-Timestamp");
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return fail(400, "时间戳格式错误");
        }

        // 时间戳有效期 5 分钟
        if (Math.abs(System.currentTimeMillis() - timestamp) > 5 * 60 * 1000) {
            return fail(403, "请求已过期，时间戳超过5分钟");
        }

        // 执行注册
        registrationService.register(request);
        return ok("注册成功，已同步业务/服务类型配置");
    }
}