package com.leantech.agent.controller;

import com.leantech.agent.model.ApiResult;
import com.leantech.agent.service.HospitalRouteService;
import com.leantech.agent.util.SignValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {

    @Autowired
    private HospitalRouteService routeService;

    protected String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        throw new IllegalStateException("用户未登录或认证信息无效");
    }

    protected String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("OPERATOR");
        }
        return "OPERATOR";
    }

    // 统一响应简化方法
    protected <T> ApiResult<T> ok(T data) {
        return ApiResult.success(data);
    }

    protected <T> ApiResult<T> ok(String message, T data) {
        return ApiResult.success(message, data);
    }

    protected <T> ApiResult<T> fail(int code, String message) {
        return ApiResult.error(code, message);
    }

    protected <T> ApiResult<T> fail(String message) {
        return ApiResult.error(message);
    }

    /**
     * 验证医院端请求签名
     * @param orgCode       机构代码
     * @param sign          请求头 X-API-Sign
     * @param timestampStr  请求头 X-API-Timestamp
     * @return 成功返回 null，失败返回 ApiResult 错误对象
     */
    protected <T> ApiResult<T> validateHospitalSign(String orgCode, String sign, String timestampStr) {
        if (orgCode == null || orgCode.isEmpty()) {
            return fail(400, "缺少机构代码");
        }
        // 从路由服务获取该机构的 API Key
        String apiKey = routeService.getHospitalApiKey(orgCode);
        if (apiKey == null || apiKey.isEmpty()) {
            return fail(403, "机构未配置API密钥");
        }
        if (!SignValidator.validate(apiKey, orgCode, sign, timestampStr)) {
            return fail(403, "签名验证失败，请检查 API Key 或时间戳是否有效");
        }
        return null; // 验证通过
    }
}