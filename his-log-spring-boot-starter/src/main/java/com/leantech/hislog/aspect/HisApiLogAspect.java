package com.leantech.hislog.aspect;

import com.alibaba.fastjson2.JSON;
import com.leantech.hislog.annotation.HisApiLog;
import com.leantech.hislog.entity.UserOperationLogDetailEntity;
import com.leantech.hislog.entity.UserOperationLogEntity;
import com.leantech.hislog.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
public class HisApiLogAspect {

    private static final Logger log = LoggerFactory.getLogger(HisApiLogAspect.class);

    // 硬编码约定值
    private static final String USER_ATTRIBUTE_KEY = "XCX_LOGIN_INFO";
    private static final String USER_ID_FIELD = "userId";
    private static final String TRACE_HEADER_NAME = "x-trace";

    private final OperationLogService operationLogService;

    // 从环境变量注入机构代码
    @Value("${th.organizationcode:UNKNOWN}")
    private String organizationCode;

    public HisApiLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(hisApiLog)")
    public Object around(ProceedingJoinPoint joinPoint, HisApiLog hisApiLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String responseStatus = "SUCCESS";
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            responseStatus = "FAILURE";
            throw e;
        } finally {
            long costMs = System.currentTimeMillis() - startTime;
            try {
                saveLog(joinPoint, hisApiLog, result, responseStatus, costMs);
            } catch (Exception e) {
                log.error("保存 HIS 调用日志失败", e);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, HisApiLog hisApiLog,
                         Object result, String responseStatus, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String hisApiName = className + "." + methodName;

        String orgCode = organizationCode;   // 从配置注入
        String userId = null;
        String loginId = null;

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                // 提取 loginId：从 x-trace 头获取，取第一个 '-' 之前的部分
                String traceHeader = request.getHeader(TRACE_HEADER_NAME);
                if (traceHeader != null && !traceHeader.isEmpty()) {
                    loginId = traceHeader.split("-")[0];
                } else {
                    loginId = request.getSession().getId(); // fallback
                }

                // 提取用户信息：从 request attribute 中获取对象，反射调用 getUserId()
                Object userObj = request.getAttribute(USER_ATTRIBUTE_KEY);
                if (userObj != null) {
                    Object uid = invokeGetter(userObj, USER_ID_FIELD);
                    if (uid != null) {
                        userId = uid.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取用户信息失败: {}", e.getMessage());
        }

        // 防御性兜底
        if (orgCode == null || orgCode.isEmpty()) orgCode = "UNKNOWN";
        if (userId == null || userId.isEmpty()) userId = "UNKNOWN";
        if (loginId == null || loginId.isEmpty()) loginId = "UNKNOWN";

        // 构造 traceId
        String bizTraceId = orgCode + "_" + userId + "_" + loginId;
        if (hisApiLog.bizType() != null && !hisApiLog.bizType().isEmpty()) {
            bizTraceId += "_" + hisApiLog.bizType();
        }
        if (hisApiLog.subBizType() != null && !hisApiLog.subBizType().isEmpty()) {
            bizTraceId += "_" + hisApiLog.subBizType();
        }

        // 主表实体
        UserOperationLogEntity logEntity = new UserOperationLogEntity();
        logEntity.setTraceId(bizTraceId);
        logEntity.setOrgCode(orgCode);
        logEntity.setUserId(userId);
        logEntity.setLoginId(loginId);
        logEntity.setBizTypeCode(hisApiLog.bizType());
        logEntity.setSubBizCode(hisApiLog.subBizType());
        logEntity.setOperation(hisApiLog.value());
        logEntity.setHisApiName(hisApiName);
        logEntity.setResponseStatus(responseStatus);
        logEntity.setCostMs((int) costMs);
        logEntity.setLogLevel("INFO");
        logEntity.setCreatedAt(LocalDateTime.now());

        // 详情实体
        UserOperationLogDetailEntity detailEntity = new UserOperationLogDetailEntity();
        detailEntity.setRequestBody(JSON.toJSONString(joinPoint.getArgs()));
        detailEntity.setResponseBody(JSON.toJSONString(result));
        detailEntity.setCreatedAt(LocalDateTime.now());

        operationLogService.saveAsync(logEntity, detailEntity);
    }

    /**
     * 反射调用 getter 方法
     */
    private Object invokeGetter(Object obj, String fieldName) {
        try {
            String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method method = obj.getClass().getMethod(getter);
            return method.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
}